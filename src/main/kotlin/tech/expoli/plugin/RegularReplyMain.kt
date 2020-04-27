package tech.expoli.plugin

import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import java.util.*

object RegularReplyMain : PluginBase() {
    private const val configPath = "config.yml"
    private val config = loadConfig(configPath)
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private var dk_single = 0

    private val groupList by lazy {
        config.setIfAbsent("groups", mutableListOf<Long>())
        config.getLongList("groups").toMutableSet()
    }

    override fun onLoad() {
        super.onLoad()
        logger.info("onLoad")

    }

    /**
     * 检查当前时间，换了天数就把记录抽签的Map清楚掉
     */

    override fun onEnable() {
        super.onEnable()
        registerCommands()

        logger.info("Plugin enabled!")

        subscribeGroupMessages {
            (contains("打卡")){
                //logger.info(senderName + "抽签")
                val dayTemp = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                if (dayTemp > day) {
                    day = dayTemp
                    dk_single = 0
                } else if (groupList.contains(this.group.id) && dk_single == 0) {
                    this.reply("已打卡")
                    dk_single = 1
                }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("Disabling")
        config["groups"] = groupList.toList()
        config.save()
        logger.info("Saving config")
    }

    // 注册命令
    private fun registerCommands() {
        registerCommand {
            name = "RegularReply"
            alias = listOf("DK")
            description = "自动打卡插件命令管理"
            usage = "[/DK enable 群号] 打开指定群的触发打卡功能\n" +
                    "[/DK disable 群号] 关闭指定群的触发打卡功能"

            onCommand {
                if (it.isEmpty()) {
                    return@onCommand false
                }
                when (it[0].toLowerCase()) {
                    "enable" -> {
                        val groupID: Long = it[1].toLong()
                        groupList.add(groupID)
                        this.sendMessage("群${groupID}:已开启打卡功能")
                        return@onCommand true
                    }
                    "disable" -> {
                        val groupID = it[1].toLong()
                        groupList.remove(groupID)
                        this.sendMessage("群${groupID}:已关闭打卡功能")
                        return@onCommand true
                    }
                    else -> {
                        return@onCommand false
                    }
                }
            }
        }
    }
}