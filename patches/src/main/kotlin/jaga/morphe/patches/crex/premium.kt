package jaga.morphe.patches.crex

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

val SubscriptionExpiredFingerprint = Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    strings = listOf(
        "yyyy-MM-dd",
        "expiry_date",
        "0000-00-00",
    ),
    custom = { method, _ ->
        val implementation = method.implementation ?: return@Fingerprint false

        val referencedStrings = implementation.instructions
            .mapNotNull { instruction ->
                (instruction as? ReferenceInstruction)
                    ?.reference
                    ?.takeIf { it is StringReference }
                    ?.toString()
            }

        referencedStrings.count { it == "yyyy-MM-dd" } == 2 &&
                referencedStrings.count { it == "0000-00-00" } == 3 &&
                referencedStrings.count { it == "expiry_date" } == 1
    }
)

@Suppress("unused")
val crexPremiumPatch = bytecodePatch(
    name = "CREX Premium",
    description = "Unlocks premium features and removes ads."
) {
    compatibleWith(Compatibility(
        name = "CREX - Just Cricket",
        packageName = "in.cricketexchange.app.cricketexchange",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xA8805C,
        targets = listOf(
            AppTarget(version = "26.04.05")
        )
    ))

    execute {
        SubscriptionExpiredFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}