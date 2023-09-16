package com.base.baseui.widget.others.radar.util

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi

data class PointConfig(var x: Float, var k: Float, var b: Float)

fun getHexagonRadius(x: Float): Float {
    val map = listOf(
        PointConfig(1000f, 0f, 6f),
        PointConfig(500f, 0.002f, 4f),
        PointConfig(200f, 0.003f, 3.33f),
        PointConfig(50f, 0.007f, 2.67f),
        PointConfig(10f, 0.025f, 1.75f),
        PointConfig(1f, 0.111f, 0.889f),
        PointConfig(0f, 1f, 0f)
    )

    var curP = map.last()
    for (p in map) {
        if (x >= p.x) {
            curP = p
            break
        }
    }

    return (curP.k * x + curP.b) / 6.0f
}

data class PointColorConfig(var x: Float, var k_r: Float, var b_r: Float, var k_g: Float, var b_g: Float, var k_b: Float, var b_b: Float)
@RequiresApi(Build.VERSION_CODES.O)
fun getLinearGradientColor(x: Float):  Int {

    if (x >= 1000) {
        val r = 233.0f / 255.0
        val g = 39.0f / 255.0
        val b = 39.0f / 255.0

        return Color.valueOf(r.toFloat(), g.toFloat(), b.toFloat()).toArgb()
    }

    val map = listOf(
        PointColorConfig(1000.0f, - 0.044f, 277.0f, -0.23f, 269.0f,  0.078f, -39.0f),
        PointColorConfig(500.0f, -0.044f,  277.0f, -0.23f, 269.0f, 0.078f, -39.0f),
        PointColorConfig(200.0f, 0.373f, 68.333f, 0.263f, 22.333f, -0.793f, 396.667f),
        PointColorConfig(50.0f, 0.567f, 29.667f, -0.393f, 153.667f, -0.113f,  260.667f),
        PointColorConfig(10.0f, -0.65f, 90.5f, -2.5f, 259.0f, 1.025f, 203.75f),
        PointColorConfig(1.0f, -5.556f, 139.556f, 0.444f, 229.556f, 15.0f, 64.0f),
        PointColorConfig(0.0f, 242.0f, 0.0f, 243.0f, 0.0f, 245.0f, 0.0f)
    )

    var curP = map.last()
    for (p in map) {
        if (x >= p.x) {
            curP = p
            break
        }
    }

    val r = (curP.k_r * x + curP.b_r) / 255.0
    val g = (curP.k_g * x + curP.b_g) / 255.0
    val b = (curP.k_b * x + curP.b_b) / 255.0

    return Color.valueOf(r.toFloat(), g.toFloat(), b.toFloat()).toArgb()
}