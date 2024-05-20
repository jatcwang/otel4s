/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.sdk

import cats.Applicative
import cats.Parallel
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.CommonConfigKeys
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ExportersAutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.TelemetryResourceAutoConfigure
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.autoconfigure.MeterProviderAutoConfigure
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.autoconfigure.ContextPropagatorsAutoConfigure
import org.typelevel.otel4s.sdk.trace.autoconfigure.TracerProviderAutoConfigure
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerProvider

final class OpenTelemetrySdk[F[_]] private (
    val meterProvider: MeterProvider[F],
    val tracerProvider: TracerProvider[F],
    val propagators: ContextPropagators[Context]
)(implicit val localContext: LocalContext[F])
    extends Otel4s[F] {

  type Ctx = Context

  override def toString: String =
    s"OpenTelemetrySdk{meterProvider=$meterProvider, tracerProvider=$tracerProvider, propagators=$propagators}"
}

object OpenTelemetrySdk {

  /** Autoconfigures [[OpenTelemetrySdk]] using [[AutoConfigured.Builder]].
    *
    * @note
    *   the external components (e.g. OTLP exporter) must be registered
    *   manually. Add the `otel4s-sdk-exporter` dependency to the sbt file:
    *   {{{
    * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
    *   }}}
    *   and register the configurer manually:
    *   {{{
    * import org.typelevel.otel4s.sdk.OpenTelemetrySdk
    * import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
    *
    * OpenTelemetrySdk.autoConfigured[IO](_.addExportersConfigurer(OtlpExporterAutoConfigure[IO]))
    *   }}}
    *
    * @param customize
    *   a function for customizing the auto-configured SDK builder
    */
  def autoConfigured[F[_]: Async: Parallel: Console: LocalContextProvider](
      customize: AutoConfigured.Builder[F] => AutoConfigured.Builder[F] =
        (a: AutoConfigured.Builder[F]) => a
  ): Resource[F, AutoConfigured[F]] =
    customize(AutoConfigured.builder[F]).build

  /** Creates a no-op implementation of the [[OpenTelemetrySdk]].
    */
  def noop[F[_]: Applicative: LocalContextProvider]: F[OpenTelemetrySdk[F]] =
    for {
      local <- LocalProvider[F, Context].local
    } yield new OpenTelemetrySdk[F](
      MeterProvider.noop,
      TracerProvider.noop,
      ContextPropagators.noop
    )(local)

  /** The auto-configured [[OpenTelemetrySdk]].
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md]]
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  sealed trait AutoConfigured[F[_]] {

    /** The auto-configured OpenTelemetry SDK.
      */
    def sdk: OpenTelemetrySdk[F]

    /** The resource the SDK was auto-configured for.
      */
    def resource: TelemetryResource

    /** The config the SDK was auto-configured with.
      */
    def config: Config
  }

  object AutoConfigured {

    type Customizer[A] = (A, Config) => A

    sealed trait Builder[F[_]] {

      /** Sets the given config to use when resolving properties.
        *
        * @note
        *   [[addPropertiesLoader]] and [[addPropertiesCustomizer]] will have no
        *   effect if the custom config is provided.
        *
        * @param config
        *   the config to use
        */
      def withConfig(config: Config): Builder[F]

      /** Adds the properties loader. Multiple loaders will be added. The loaded
        * properties will be merged with the default config. Loaded properties
        * take precedence over the default ones.
        *
        * @param loader
        *   the additional loader to add
        */
      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F]

      /** Adds the properties customizer. Multiple customizers can be added, and
        * they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F]

      /** Adds the meter provider builder customizer. Multiple customizers can
        * be added, and they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addMeterProviderCustomizer(
          customizer: Customizer[SdkMeterProvider.Builder[F]]
      ): Builder[F]

      /** Adds the tracer provider builder customizer. Multiple customizers can
        * be added, and they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F]

      /** Adds the telemetry resource customizer. Multiple customizers can be
        * added, and they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F]

      /** Adds both metric and span exporter configurers. Can be used to
        * register exporters that aren't included in the SDK.
        *
        * @example
        *   Add the `otel4s-sdk-exporter` dependency to the build file:
        *   {{{
        * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        *   }}}
        *   and register the configurer manually:
        *   {{{
        * import org.typelevel.otel4s.sdk.OpenTelemetrySdk
        * import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExporterAutoConfigure
        *
        * OpenTelemetrySdk.autoConfigured[IO](_.addExporterConfigurer(OtlpExporterAutoConfigure[IO]))
        *   }}}
        *
        * @param configurer
        *   the configurer to add
        */
      def addExportersConfigurer(
          configurer: ExportersAutoConfigure[F]
      ): Builder[F]

      /** Adds the exporter configurer. Can be used to register exporters that
        * aren't included in the SDK.
        *
        * @example
        *   Add the `otel4s-sdk-exporter` dependency to the build file:
        *   {{{
        * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        *   }}}
        *   and register the configurer manually:
        *   {{{
        * import org.typelevel.otel4s.sdk.OpenTelemetrySdk
        * import org.typelevel.otel4s.sdk.exporter.otlp.metrics.autoconfigure.OtlpMetricExporterAutoConfigure
        *
        * OpenTelemetrySdk.autoConfigured[IO](_.addMetricExporterConfigurer(OtlpMetricExporterAutoConfigure[IO]))
        *   }}}
        *
        * @param configurer
        *   the configurer to add
        */
      def addMetricExporterConfigurer(
          configurer: AutoConfigure.Named[F, MetricExporter[F]]
      ): Builder[F]

      /** Adds the exporter configurer. Can be used to register exporters that
        * aren't included in the SDK.
        *
        * @example
        *   Add the `otel4s-sdk-exporter` dependency to the build file:
        *   {{{
        * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        *   }}}
        *   and register the configurer manually:
        *   {{{
        * import org.typelevel.otel4s.sdk.OpenTelemetrySdk
        * import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
        *
        * OpenTelemetrySdk.autoConfigured[IO](_.addSpanExporterConfigurer(OtlpSpanExporterAutoConfigure[IO]))
        *   }}}
        *
        * @param configurer
        *   the configurer to add
        */
      def addSpanExporterConfigurer(
          configurer: AutoConfigure.Named[F, SpanExporter[F]]
      ): Builder[F]

      /** Adds the sampler configurer. Can be used to register samplers that
        * aren't included in the SDK.
        *
        * @param configurer
        *   the configurer to add
        */
      def addSamplerConfigurer(
          configurer: AutoConfigure.Named[F, Sampler]
      ): Builder[F]

      /** Adds the text map propagator configurer. Can be used to register
        * propagators that aren't included in the SDK.
        *
        * @param configurer
        *   the configurer to add
        */
      def addTextMapPropagatorConfigurer(
          configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]
      ): Builder[F]

      /** Creates [[OpenTelemetrySdk]] using the configuration of this builder.
        */
      def build: Resource[F, AutoConfigured[F]]
    }

    /** Creates a [[Builder]].
      */
    def builder[
        F[_]: Async: Parallel: Console: LocalContextProvider
    ]: Builder[F] =
      BuilderImpl(
        customConfig = None,
        propertiesLoader = Async[F].pure(Map.empty),
        propertiesCustomizers = Nil,
        resourceCustomizer = (a, _) => a,
        meterProviderCustomizer = (a: SdkMeterProvider.Builder[F], _) => a,
        tracerProviderCustomizer = (a: SdkTracerProvider.Builder[F], _) => a,
        metricExporterConfigurers = Set.empty,
        spanExporterConfigurers = Set.empty,
        samplerConfigurers = Set.empty,
        textMapPropagatorConfigurers = Set.empty
      )

    private final case class BuilderImpl[
        F[_]: Async: Parallel: Console: LocalContextProvider
    ](
        customConfig: Option[Config],
        propertiesLoader: F[Map[String, String]],
        propertiesCustomizers: List[Config => Map[String, String]],
        resourceCustomizer: Customizer[TelemetryResource],
        meterProviderCustomizer: Customizer[SdkMeterProvider.Builder[F]],
        tracerProviderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
        metricExporterConfigurers: Set[
          AutoConfigure.Named[F, MetricExporter[F]]
        ],
        spanExporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]],
        samplerConfigurers: Set[AutoConfigure.Named[F, Sampler]],
        textMapPropagatorConfigurers: Set[
          AutoConfigure.Named[F, TextMapPropagator[Context]]
        ]
    ) extends Builder[F] {

      def withConfig(config: Config): Builder[F] =
        copy(customConfig = Some(config))

      def addPropertiesLoader(
          loader: F[Map[String, String]]
      ): Builder[F] =
        copy(propertiesLoader = (this.propertiesLoader, loader).mapN(_ ++ _))

      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F] =
        copy(propertiesCustomizers = this.propertiesCustomizers :+ customizer)

      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F] =
        copy(resourceCustomizer = merge(this.resourceCustomizer, customizer))

      def addMeterProviderCustomizer(
          customizer: Customizer[SdkMeterProvider.Builder[F]]
      ): Builder[F] =
        copy(meterProviderCustomizer =
          merge(this.meterProviderCustomizer, customizer)
        )

      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F] =
        copy(tracerProviderCustomizer =
          merge(this.tracerProviderCustomizer, customizer)
        )

      def addExportersConfigurer(
          configurer: ExportersAutoConfigure[F]
      ): Builder[F] =
        copy(
          metricExporterConfigurers =
            metricExporterConfigurers + configurer.metricExporterAutoConfigure,
          spanExporterConfigurers =
            spanExporterConfigurers + configurer.spanExporterAutoConfigure
        )

      def addMetricExporterConfigurer(
          configurer: AutoConfigure.Named[F, MetricExporter[F]]
      ): Builder[F] =
        copy(metricExporterConfigurers = metricExporterConfigurers + configurer)

      def addSpanExporterConfigurer(
          configurer: AutoConfigure.Named[F, SpanExporter[F]]
      ): Builder[F] =
        copy(spanExporterConfigurers = spanExporterConfigurers + configurer)

      def addSamplerConfigurer(
          configurer: AutoConfigure.Named[F, Sampler]
      ): Builder[F] =
        copy(samplerConfigurers = samplerConfigurers + configurer)

      def addTextMapPropagatorConfigurer(
          configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]
      ): Builder[F] =
        copy(textMapPropagatorConfigurers =
          textMapPropagatorConfigurers + configurer
        )

      def build: Resource[F, AutoConfigured[F]] = {
        def loadConfig: F[Config] =
          for {
            props <- propertiesLoader
            config <- Config.load(props)
          } yield propertiesCustomizers.foldLeft(config)((cfg, c) =>
            cfg.withOverrides(c(cfg))
          )

        def loadNoop(config: Config): Resource[F, AutoConfigured[F]] =
          Resource.eval(
            for {
              _ <- Console[F].println(
                s"OpenTelemetrySdk: the '${CommonConfigKeys.SdkDisabled}' set to 'true'. Using no-op implementation"
              )
              sdk <- OpenTelemetrySdk.noop[F]
              resource = TelemetryResource.empty
            } yield Impl(sdk, resource, config)
          )

        def loadSdk(
            config: Config,
            resource: TelemetryResource
        ): Resource[F, AutoConfigured[F]] = {
          def makeLocalContext = LocalProvider[F, Context].local

          val traceContextLookup: TraceContextLookup =
            new TraceContextLookup {
              def get(context: Context): Option[ExemplarData.TraceContext] =
                context
                  .get(SdkContextKeys.SpanContextKey)
                  .filter(_.isValid)
                  .map { ctx =>
                    ExemplarData.TraceContext(
                      ctx.traceId,
                      ctx.spanId,
                      ctx.isSampled
                    )
                  }
            }

          Resource.eval(makeLocalContext).flatMap { implicit local =>
            Resource.eval(Random.scalaUtilRandom).flatMap { implicit random =>
              val propagatorsConfigure = ContextPropagatorsAutoConfigure[F](
                textMapPropagatorConfigurers
              )

              propagatorsConfigure.configure(config).flatMap { propagators =>
                val meterProviderConfigure = MeterProviderAutoConfigure[F](
                  resource,
                  traceContextLookup,
                  meterProviderCustomizer,
                  metricExporterConfigurers
                )

                val tracerProviderConfigure = TracerProviderAutoConfigure[F](
                  resource,
                  propagators,
                  tracerProviderCustomizer,
                  samplerConfigurers,
                  spanExporterConfigurers
                )

                for {
                  meterProvider <- meterProviderConfigure.configure(config)
                  tracerProvider <- tracerProviderConfigure.configure(config)
                  sdk = new OpenTelemetrySdk(
                    meterProvider,
                    tracerProvider,
                    propagators
                  )
                } yield Impl(sdk, resource, config)
              }
            }
          }
        }

        for {
          config <- Resource.eval(customConfig.fold(loadConfig)(Async[F].pure))

          resource <- TelemetryResourceAutoConfigure[F]
            .configure(config)
            .map(resourceCustomizer(_, config))

          isDisabled <- Resource.eval(
            Async[F].fromEither(
              config.getOrElse(CommonConfigKeys.SdkDisabled, false)
            )
          )

          sdk <- if (isDisabled) loadNoop(config) else loadSdk(config, resource)
        } yield sdk
      }

      private def merge[A](
          first: Customizer[A],
          second: Customizer[A]
      ): Customizer[A] =
        (a, config) => second(first(a, config), config)

    }

    private final case class Impl[F[_]](
        sdk: OpenTelemetrySdk[F],
        resource: TelemetryResource,
        config: Config
    ) extends AutoConfigured[F] {
      override def toString: String =
        s"OpenTelemetrySdk.AutoConfigured{sdk=$sdk, resource=$resource}"
    }
  }

}
