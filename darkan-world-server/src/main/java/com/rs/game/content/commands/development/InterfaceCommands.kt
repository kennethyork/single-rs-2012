package com.rs.game.content.commands.development

import com.rs.Settings
import com.rs.cache.loaders.NPCDefinitions
import com.rs.engine.command.Commands
import com.rs.game.content.achievements.Achievement
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.lib.game.Rights
import com.rs.lib.util.Utils
import com.rs.lib.util.Utils.clampI
import com.rs.plugin.annotations.ServerStartupEvent

private val UNIDENTIFIED_ANIMS = intArrayOf(2057, 12549, 15461, 3024, 2202, 2205, 2200, 2212, 2207, 2211, 2208, 2197, 2195, 15719, 3145, 4122, 3874, 587, 10707, 1107, 2938, 2435, 2438, 2434, 2552, 2449, 2447, 14165, 2976, 2932, 3320, 3911, 5964, 3777, 3727, 2825, 2826, 16254, 16355, 16366, 16363, 16382, 16393, 10371, 1599, 3853, 3452, 3471, 3423, 3348, 2048, 3982, 2149, 6083, 2148, 2788, 11620, 2614, 15138, 12033, 3071, 13691, 2764, 12919, 1409, 6703, 15624, 2450, 1354, 15622, 2378, 9104, 16445, 654, 3032, 3670, 15147, 14175, 9021, 12913, 4471, 1852, 11146, 3266, 1633, 15128, 2343, 15203, 2010,
    4223, 9899, 2903, 2326, 1307, 2891, 15139, 1196, 1895, 1485, 2697, 1267, 4611, 1914, 1606, 2094, 2813, 2793, 3182, 12806, 11604, 1317, 2684, 3562, 3308, 1155, 2138, 1855, 1867, 1255, 3093, 9723, 1893, 786, 3458, 3454, 3456, 3455, 3453, 2008, 2007, 2002, 3166, 2767, 11224, 5767, 3092, 1356, 1361, 1006, 948, 3546, 9681, 1492, 646, 1136, 345, 6, 1898, 2159, 120, 1418, 9946, 166, 179, 14844, 3437, 232, 16322, 296, 9502, 1017, 3575, 3574, 3681, 3601, 3588, 3612, 3650, 14289, 3120, 3122, 2998, 3576, 10243, 1870, 3098, 1621, 1620, 3123, 1922, 3037, 1452, 1456, 3474, 3344, 1998, 2000,
    2030, 1761, 1410, 2330, 1095, 3189, 2897, 1232, 1213, 1219, 1498, 1279, 1635, 2808, 3987, 3978, 1876, 2571, 1262, 2103, 455, 2711, 3220, 466, 443, 1247, 450, 730, 3229, 463, 458, 456, 2166, 1546, 1562, 3164, 1815, 5013, 4019, 4016, 4029, 4135, 4138, 15080, 4156, 4164, 15113, 15129, 4222, 6689, 4262, 4253, 4281, 4258, 4283, 4285, 4316, 4333, 9164, 4337, 4347, 4352, 4404, 4359, 4364, 4361, 4434, 4576, 4573, 4834, 4553, 7139, 4574, 4592, 7033, 12609, 4600, 4752, 4749, 4822, 4818, 4803, 4796, 4909, 4912, 4892, 14242, 4904, 4908, 4903, 4878, 4907, 4863, 8525, 5015, 5054, 15137, 5421,
    5076, 5077, 5053, 5139, 5157, 5154, 5266, 5299, 5307, 14302, 5360, 5354, 5062, 8849, 5420, 2757, 9405, 5599, 5588, 6104, 10355, 9577, 5736, 5733, 6364, 5796, 5812, 5813, 6147, 5861, 5905, 5902, 5904, 5908, 6073, 4219, 6125, 11411, 6121, 6213, 6179, 6169, 4852, 6459, 11608, 6273, 15011, 452, 12217, 6482, 11739, 6427, 6452, 6409, 6530, 6554, 6611, 6599, 6665, 6700, 6636, 6644, 6641, 6640, 14723, 11540, 6851, 12446, 6860, 14174, 6899, 6921, 6920, 9387, 6941, 7024, 16420, 1016, 16421, 16419, 15238, 6981, 7113, 7130, 7136, 7137, 7141, 7135, 7119, 7138, 7114, 7116, 7145, 35, 7233, 7240,
    5076, 5077, 5053, 5139, 5157, 5154, 5266, 5299, 5307, 14302, 5360, 5354, 5062, 8849, 5420, 2757, 9405, 5599, 5588, 6104, 10355, 9577, 5736, 5733, 6364, 5796, 5812, 5813, 6147, 5861, 5905, 5902, 5904, 5908, 6073, 4219, 6125, 11411, 6121, 6213, 6179, 6169, 4852, 6459, 11608, 6273, 15011, 452, 12217, 6482, 11739, 6427, 6452, 6409, 6530, 6554, 6611, 6599, 6665, 6700, 6636, 6644, 6641, 6640, 14723, 11540, 6851, 12446, 6860, 14174, 6899, 6921, 6920, 9387, 6941, 7024, 16420, 1016, 16421, 16419, 15238, 6981, 7113, 7130, 7136, 7137, 7141, 7135, 7119, 7138, 7114, 7116, 7145, 35, 7233, 7240,
    7253, 7281, 7293, 7278, 7290, 7277, 7296, 7299, 7321, 7348, 7352, 7359, 8806, 8859, 7388, 7489, 9610, 9633, 7538, 8450, 7545, 7551, 8535, 7634, 7652, 8402, 8474, 8466, 8418, 8486, 8434, 8379, 8414, 8430, 8422, 10554, 8426, 8494, 8482, 8490, 8446, 8462, 8470, 8137, 8509, 8662, 8590, 8645, 8625, 486, 11451, 8699, 1221, 1220, 1228, 8711, 8748, 8746, 8747, 8744, 8768, 8803, 8826, 8895, 8884, 10475, 10992, 11568, 11552, 8991, 9018, 10449, 9861, 9056, 9043, 9034, 10121, 15135, 9148, 9147, 9142, 9149, 4294, 12397, 9224, 9319, 9245, 9232, 9238, 9251, 9257, 9269, 15142, 9327, 9328, 9351,
    12187, 9384, 9427, 9391, 9389, 9397, 9513, 9574, 9543, 9582, 9982, 9909, 15416, 4856, 12373, 3329, 9854, 13656, 16301, 6830, 10024, 9971, 9972, 9966, 9990, 10079, 12319, 10052, 10047, 10094, 10095, 10220, 10191, 10128, 10127, 10221, 10229, 10331, 10334, 10316, 12191, 10350, 10353, 10373, 10414, 10489, 10445, 10443, 10448, 10430, 14223, 15726, 10559, 10746, 10649, 10702, 10880, 10893, 10827, 10821, 10754, 10894, 10712, 10786, 10790, 10788, 10805, 10809, 10807, 10780, 10784, 10782, 10792, 10811, 10803, 10778, 14633, 10989, 10935, 10932, 10930, 10995, 14713, 11011, 11032, 11056,
    11220, 1132, 1715, 1708, 11286, 11284, 11336, 11340, 11350, 11316, 11314, 11305, 11312, 11376, 1742, 11372, 11321, 11298, 11547, 16934, 11561, 11566, 11576, 11590, 11622, 11625, 11644, 11655, 11688, 11716, 11765, 11770, 11766, 884, 11794, 11819, 7527, 15825, 11860, 11862, 11882, 11821, 11933, 13945, 3273, 411, 11959, 3327, 12059, 12115, 12113, 12117, 12137, 12130, 12111, 12167, 12112, 16438, 12225, 12219, 12224, 12924, 12241, 12296, 12600, 12357, 12401, 12400, 12417, 12432, 12421, 12433, 12442, 12503, 12495, 12529, 12554, 12589, 12536, 12550, 1513, 12630, 1548, 12606, 4609, 12636,
    4615, 12655, 12733, 9514, 5083, 12781, 12775, 12787, 12814, 12832, 6607, 12836, 12835, 12888, 12887, 16931, 12923, 12265, 13593, 13663, 14808, 13315, 13782, 13643, 13730, 13572, 13563, 13581, 13594, 13559, 13557, 13554, 13566, 13574, 13582, 13721, 13007, 13500, 13353, 14610, 13758, 13763, 13636, 13324, 14798, 13766, 14152, 13817, 13814, 13831, 13839, 13967, 13959, 14083, 14058, 13973, 14086, 15127, 14078, 14007, 14147, 1038, 14116, 14101, 2129, 4874, 14163, 14177, 14206, 14208, 14231, 14273, 14275, 14317, 14355, 14345, 14335, 14349, 14562, 14561, 14556, 14514, 14569, 14414, 14533,
    14962, 14401, 14673, 14639, 14645, 14722, 14733, 14748, 14780, 14787, 14941, 14841, 14845, 14886, 14929, 14982, 3302, 3300, 3288, 3290, 15239, 15034, 15063, 15097, 7573, 15103, 15105, 15217, 15187, 15176, 15181, 6292, 6998, 7493, 15261, 7502, 7437, 7436, 7434, 15906, 15317, 15305, 15308, 15275, 15276, 2916, 15304, 16077, 107, 379, 307, 10825, 1478, 4207, 10860, 10824, 10116, 7456, 9573, 12465, 5848, 15353, 10964, 1174, 1291, 12437, 9674, 16875, 1202, 1730, 1786, 2120, 2566, 2231, 15740, 2605, 3413, 3536, 3497, 3052, 3929, 7055, 8644, 7157, 6787, 5684, 8498, 6066, 6575, 6366, 5784,
    6786, 4405, 9856, 4729, 9818, 9211, 9700, 9699, 9694, 9503, 9953, 11553, 16501, 12361, 11843, 10731, 10734, 11957, 11235, 16325, 10978, 12666, 12546, 12760, 12901, 12934, 15644, 13686, 13510, 13597, 13956, 13700, 14528, 14306, 16921, 14878, 14955, 15313, 15376, 15330, 15402, 15382, 15429, 15433, 15536, 15560, 15573, 15566, 15576, 15900, 15584, 15592, 15602, 15733, 15702, 15637, 15639, 15658, 15645, 15717, 15716, 16212, 15746, 15754, 15940, 16404, 15994, 15898, 15757, 16062, 15802, 15808, 15937, 16072, 8527, 15943, 8545, 16245, 16410, 16439, 16429, 16370, 16532, 16548, 16533, 16632,
    16686, 16647, 16617, 16623, 16620, 16629, 16603, 16671, 16940, 16929, 16870, 16826, 16835, 16855, 16825, 16770, 16767, 16760, 16850, 16864, 16881, 16917, 16894, 16935, 16938, 16973, 16964, 16958, 16978, 16987, 17064, 17010, 17132, 17118, 17159, 17184, 17169, 17155, 17149, 17158, 17168)


@ServerStartupEvent
fun loadInterfaceCommands() {
    Commands.add(Rights.DEVELOPER, "dial [npcId animId]", "Dialogue box") { p, args ->
        p.interfaceManager.sendChatBoxInterface(1184)
        p.packets.setIFText(1184, 17, NPCDefinitions.getDefs(args[0].toInt()).name)
        p.packets.setIFText(1184, 13, "How dare you!")
        p.packets.setIFNPCHead(1184, 11, args[0].toInt())
        p.packets.setIFAnimation(args[1].toInt(), 1184, 11)
    }

    Commands.add(Rights.DEVELOPER, "dialrot [npcId next/prev/start_num]", "Dialogue box animation rotation") { p, args ->
        var idx = p.tempAttribs.getI("tempDialCheck", 0)

        if (args[1].matches(Regex("[0-9]+"))) {
            idx = args[1].toInt() + 1
        }

        val anim = UNIDENTIFIED_ANIMS[idx]
        p.interfaceManager.sendChatBoxInterface(1184)
        p.packets.setIFText(1184, 17, NPCDefinitions.getDefs(args[0].toInt()).name)
        p.packets.setIFText(1184, 13, "DICK!")
        p.packets.setIFNPCHead(1184, 11, args[0].toInt())
        p.packets.setIFAnimation(anim, 1184, 11)
        p.sendMessage("Anim: $anim")

        when (args[1]) {
            "next" -> p.tempAttribs.setI("tempDialCheck", clampI(idx + 1, 0, UNIDENTIFIED_ANIMS.size - 1))
            else -> p.tempAttribs.setI("tempDialCheck", clampI(idx - 1, 0, UNIDENTIFIED_ANIMS.size - 1))
        }

        p.packets.sendDevConsoleMessage("$idx/${UNIDENTIFIED_ANIMS.size}")
    }

    Commands.add(Rights.DEVELOPER, "icompanim [interfaceId componentId animId]", "Plays animation id on interface component.") { p, args ->
        p.interfaceManager.sendInterface(args[0].toInt())
        p.packets.setIFAnimation(args[2].toInt(), args[0].toInt(), args[1].toInt())
    }

    Commands.add(Rights.DEVELOPER, "icompanimrot [interfaceId componentId next/prev]", "Rotates animations on an interface component.") { p, args ->
        var idx = p.tempAttribs.getI("tempDialCheck", 0)
        val anim = UNIDENTIFIED_ANIMS[idx]

        p.interfaceManager.sendInterface(args[0].toInt())
        p.packets.setIFAnimation(anim, args[0].toInt(), args[1].toInt())
        p.sendMessage("Anim: $anim")

        when (args[2]) {
            "next" -> p.tempAttribs.setI("tempDialCheck", clampI(idx + 1, 0, UNIDENTIFIED_ANIMS.size - 1))
            else -> p.tempAttribs.setI("tempDialCheck", clampI(idx - 1, 0, UNIDENTIFIED_ANIMS.size - 1))
        }

        p.packets.sendDevConsoleMessage("$idx/${UNIDENTIFIED_ANIMS.size}")
    }

    val interfaceRights = if (Settings.getConfig().isDebug) Rights.PLAYER else Rights.ADMIN

    Commands.add(interfaceRights, "inter [interfaceId]", "Opens an interface with specific ID.") { p, args ->
        p.interfaceManager.sendInterface(args[0].toInt())
    }

    Commands.add(Rights.DEVELOPER, "winter [interfaceId componentId]", "Sends an interface to the window specified component.") { p, args ->
        val topInterface = if (p.resizeable()) InterfaceManager.RESIZEABLE_TOP else InterfaceManager.FIXED_TOP

        if (args[1].toInt() > Utils.getInterfaceDefinitionsComponentsSize(topInterface)) {
            return@add
        }

        val prevCmd = p.nsv.getI("prevWinterCmd", -1)
        if (prevCmd != -1) {
            p.interfaceManager.removeGameWindowSub(prevCmd, prevCmd)
        }

        p.nsv.setI("prevWinterCmd", args[1].toInt())
        p.interfaceManager.sendGameWindowSub(args[1].toInt(), args[1].toInt(), args[0].toInt(), true)
    }

    Commands.add(Rights.ADMIN, "istrings [interfaceId]", "Debugs an interface's text components.") { p, args ->
        val interId = args[0].toInt()
        p.interfaceManager.sendInterface(interId)

        for (componentId in 0 until Utils.getInterfaceDefinitionsComponentsSize(interId)) {
            p.packets.setIFText(interId, componentId, componentId.toString())
        }
    }

    Commands.add(Rights.DEVELOPER, "iftext [interfaceId componentId text]", "Sets the text of an interface.") { p, args ->
        val interId = args[0].toInt()
        val compId = args[1].toInt()
        val text = args[2]

        p.interfaceManager.sendInterface(interId)
        p.packets.setIFText(interId, compId, text)
    }

    Commands.add(Rights.DEVELOPER, "ifgraphic [interfaceId componentId graphicId]", "Sets the graphic of an interface.") { p, args ->
        val interId = args[0].toInt()
        val compId = args[1].toInt()
        val graphicId = args[2].toInt()

        p.packets.setIFGraphic(interId, compId, graphicId)
    }

    Commands.add(Rights.DEVELOPER, "imodels [interfaceId]", "Debugs an interface's models.") { p, args ->
        val interId = args[0].toInt()
        p.interfaceManager.sendInterface(interId)

        for (componentId in 0 until Utils.getInterfaceDefinitionsComponentsSize(interId)) {
            p.packets.setIFModel(interId, componentId, 66)
        }
    }

    Commands.add(Rights.DEVELOPER, "imodel [interfaceId componentId modelId]", "Sends a model to an interface") { p, args ->
        val interId = args[0].toInt()
        val compId = args[1].toInt()

        if (compId > Utils.getInterfaceDefinitionsComponentsSize(interId)) {
            p.sendMessage("There are ${Utils.getInterfaceDefinitionsComponentsSize(interId)} components. Too high.")
            return@add
        }

        p.interfaceManager.sendInterface(interId)
        p.packets.setIFModel(interId, compId, args[2].toInt())
    }

    Commands.add(Rights.DEVELOPER, "hidec [interfaceId componentId hidden]", "Show/hide component.") { p, args ->
        p.packets.setIFHidden(args[0].toInt(), args[1].toInt(), args[2].toBoolean())
    }

    Commands.add(Rights.DEVELOPER, "pin", "Opens bank pin interface.") { p, _ ->
        p.bank.openPin()
    }

    Commands.add(Rights.DEVELOPER, "cutscene [id]", "Plays a predefined cutscene") { p, args ->
        p.packets.sendCutscene(args[0].toInt())
    }

    Commands.add(Rights.DEVELOPER, "cheev [id]", "Sends achievement complete interface.") { p, args ->
        p.interfaceManager.sendAchievementComplete(Achievement.forId(args[0].toInt()))
    }
}