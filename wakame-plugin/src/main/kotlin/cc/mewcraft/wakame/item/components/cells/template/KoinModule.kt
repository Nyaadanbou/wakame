package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.components.ElementSampleNodeReader
import cc.mewcraft.wakame.item.components.KizamiSampleNodeReader
import cc.mewcraft.wakame.item.components.cells.template.cores.templateCoresModule
import cc.mewcraft.wakame.item.components.cells.template.curses.templateCursesModule
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiSerializer
import cc.mewcraft.wakame.random3.SampleNodeFacade
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal const val TEMPLATE_CORE_SAMPLE_NODE_READER = "template_core_sample_node_reader"
internal const val TEMPLATE_CORE_SAMPLE_NODE_FACADE = "template_core_sample_node_facade"
internal const val TEMPLATE_CURSE_SAMPLE_NODE_READER = "template_curse_sample_node_reader"
internal const val TEMPLATE_CURSE_SAMPLE_NODE_FACADE = "template_curse_sample_node_facade"
internal const val TEMPLATE_ELEMENT_SAMPLE_NODE_READER = "template_element_sample_node_reader"
internal const val TEMPLATE_ELEMENT_SAMPLE_NODE_FACADE = "template_element_sample_node_facade"
internal const val TEMPLATE_KIZAMI_SAMPLE_NODE_READER = "template_kizami_sample_node_reader"
internal const val TEMPLATE_KIZAMI_SAMPLE_NODE_FACADE = "template_kizami_sample_node_facade"

internal fun cellsTemplateModule(): Module = module {
    includes(
        templateCoresModule(),
        templateCursesModule()
    )

    // Cores ...
    single<TemplateCoreSampleNodeReader>(named(TEMPLATE_CORE_SAMPLE_NODE_READER)) {
        TemplateCoreSampleNodeReader()
    }
    single<SampleNodeFacade<TemplateCore, GenerationContext>>(named(TEMPLATE_CORE_SAMPLE_NODE_FACADE)) {
        SampleNodeFacade(
            Configs.YAML.build("random/cores/entries.yml") {
                defaultOptions { opts ->
                    opts.serializers {
                        it.registerAll(get(named(ELEMENT_EXTERNALS)))
                        it.registerAll(get(named(SKILL_EXTERNALS)))
                        it.kregister(TemplateCoreSerializer)
                    }
                }
            },
            get<TemplateCoreSampleNodeReader>(named(TEMPLATE_CORE_SAMPLE_NODE_READER))
        )
    } bind Initializable::class

    // Curses ...
    single<TemplateCurseSampleNodeReader>(named(TEMPLATE_CURSE_SAMPLE_NODE_READER)) {
        TemplateCurseSampleNodeReader()
    }
    single<SampleNodeFacade<TemplateCurse, GenerationContext>>(named(TEMPLATE_CURSE_SAMPLE_NODE_FACADE)) {
        SampleNodeFacade(
            Configs.YAML.build("random/curses/entries.yml") {
                defaultOptions { opts ->
                    opts.serializers {
                        it.registerAll(get(named(ELEMENT_EXTERNALS)))
                        it.registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS)))
                        it.kregister(TemplateCurseSerializer)
                    }
                }
            },
            get<TemplateCurseSampleNodeReader>(named(TEMPLATE_CURSE_SAMPLE_NODE_READER))
        )
    } bind Initializable::class

    // Elements ...
    single<ElementSampleNodeReader>(named(TEMPLATE_ELEMENT_SAMPLE_NODE_READER)) {
        ElementSampleNodeReader()
    }
    single<SampleNodeFacade<Element, GenerationContext>>(named(TEMPLATE_ELEMENT_SAMPLE_NODE_FACADE)) {
        SampleNodeFacade(
            Configs.YAML.build("random/elements/entries.yml") {
                defaultOptions { opts ->
                    opts.serializers {
                        it.kregister(ElementSerializer)
                    }
                }
            },
            get<ElementSampleNodeReader>(named(TEMPLATE_ELEMENT_SAMPLE_NODE_READER))
        )
    } bind Initializable::class

    // Kizamiz ...
    single<KizamiSampleNodeReader>(named(TEMPLATE_KIZAMI_SAMPLE_NODE_READER)) {
        KizamiSampleNodeReader()
    }
    single<SampleNodeFacade<Kizami, GenerationContext>>(named(TEMPLATE_KIZAMI_SAMPLE_NODE_FACADE)) {
        SampleNodeFacade(
            Configs.YAML.build("random/kizamiz/entries.yml") {
                defaultOptions { opts ->
                    opts.serializers {
                        it.kregister(KizamiSerializer)
                    }
                }
            },
            get<KizamiSampleNodeReader>(named(TEMPLATE_KIZAMI_SAMPLE_NODE_READER))
        )
    } bind Initializable::class
}