package com.befitnessapp.utils

import com.befitnessapp.prefs.WeightUnit
import java.util.Locale
import kotlin.math.abs

private const val KG_TO_LB = 2.2046226f

// Si en algún lado usan todavía convertFromKg, lo conservamos
fun convertFromKg(valueKg: Float, unit: WeightUnit): Float =
    if (unit == WeightUnit.LB) valueKg * KG_TO_LB else valueKg

fun formatWeightFromKg(
    valueKg: Float,
    unit: WeightUnit
): String {
    val value = convertFromKg(valueKg, unit)
    val suffix = if (unit == WeightUnit.LB) "lb" else "kg"
    return "${formatNumber(value)} $suffix"
}

fun formatVolumeFromKg(
    valueKg: Float,
    unit: WeightUnit
): String {
    val value = convertFromKg(valueKg, unit)
    val suffix = if (unit == WeightUnit.LB) "lb·rep" else "kg·rep"
    return "${formatNumber(value)} $suffix"
}

private fun formatNumber(v: Float): String {
    val av = abs(v)
    val s = when {
        av >= 10_000f ->
            String.format(Locale.getDefault(), "%.1fk", v / 1000f)
        av >= 1_000f ->
            String.format(Locale.getDefault(), "%.0f", v)
        else ->
            String.format(Locale.getDefault(), "%.1f", v)
    }
    return if (s.endsWith(".0")) s.dropLast(2) else s
}
