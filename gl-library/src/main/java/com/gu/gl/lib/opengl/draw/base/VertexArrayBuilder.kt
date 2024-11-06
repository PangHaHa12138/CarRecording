package com.gu.gl.lib.opengl.draw.base

import android.graphics.PointF

abstract class VertexArrayBuilder(val array: FloatArray) {

    open fun build(): VertexArray {
        return VertexArray(array)
    }

    open fun adjustBorder() {}
}

class SimpleBuilder private constructor(array: FloatArray) : VertexArrayBuilder(array) {
    companion object {
        fun create(array: FloatArray): VertexArrayBuilder {
            return SimpleBuilder(array)
        }
    }
}

//根据文字的长度和字体大小，计算texture占用比例，然后修改floatArray中的值。
class TextArrayBuilder private constructor(
    array: FloatArray, private val sizeRate: PointF, private val margin: PointF, private val adjustAction: (FloatArray, PointF, PointF) -> Unit
) : VertexArrayBuilder(array) {

    companion object {
        fun create(array: FloatArray, sizeRate: PointF, margin: PointF, adjustAction: (FloatArray, PointF, PointF) -> Unit): VertexArrayBuilder {
            return TextArrayBuilder(array, sizeRate, margin, adjustAction)
        }


        /*
       -1f, (-0.9f), 0f, 0f,//左上
       -1f, -1f, 0f, 1f,//左下
       (0.5f), -1f, 1f, 1f,//右下
       -1f, (-0.9f), 0f, 0f,//左上
       (0.5f), -1f, 1f, 1f,//右下
       (0.5f), (-0.9f), 1f, 0f//右上

       -1f, (1f), 0f, 0f,//左上
       -1f, -1f, 0f, 1f,//左下
       (1f), -1f, 1f, 1f,//右下
       -1f, (1f), 0f, 0f,//左上
       (1f), -1f, 1f, 1f,//右下
       (1f), (1f), 1f, 0f//右上
       */
        //根据text内容计算FloatArray,调整texture尺寸,定位左下
        val posLeftBottom = { array: FloatArray, rate: PointF, margin: PointF ->

            val hr = -1f + rate.y//getFormatFloat(-1f + heightRate)
            val wr = -1f + rate.x//getFormatFloat(-1f + widthRate)

            array[1] = hr
            array[8] = wr
            array[13] = hr
            array[16] = wr
            array[20] = wr
            array[21] = hr
            array.forEachIndexed { index, _ ->
                if (index % 4 == 0) array[index] += margin.x
                else if (index % 4 == 1) array[index] += margin.y
            }
        }

        /*
             -1f, 1f, 0f, 0f,//左上
                -1f, (0.9f), 0f, 1f,//左下
                (0.5f), (0.9f), 1f, 1f,//右下
                -1f, 1f, 0f, 0f,//左上
                (0.5f), (0.9f), 1f, 1f,//右下
                (0.5f), 0f, 1f, 0f//右上


                (-0.5f),   1f,   0f, 0f,//左上
                (-0.5f), (0.9f), 0f, 1f,//左下
                1f,      (0.9f), 1f, 1f,//右下
                (-0.5f),  1f,    0f, 0f,//左上
                1f,      (0.9f), 1f, 1f,//右下
                1f,       1f,    1f, 0f//右上



                (-1f),   1f,  0f, 0f,//左上
                (-1f), (-1f), 0f, 1f,//左下
                 1f,   (-1f), 1f, 1f,//右下
                (-1f),   1f,  0f, 0f,//左上
                 1f,   (-1f), 1f, 1f,//右下
                 1f,     1f,  1f, 0f//右上
                */
        val posRightTop = { array: FloatArray, rate: PointF, margin: PointF ->
            margin.x = -margin.x
            margin.y = -margin.y

            val hr = 1f - rate.y//getFormatFloat(-1f + heightRate)//0.9
            val wr = 1f - rate.x//getFormatFloat(-1f + widthRate)//-0.5
            array[0] = wr
            array[4] = wr
            array[5] = hr
            array[9] = hr
            array[12] = wr
            array[17] = hr
            array.forEachIndexed { index, _ ->
                if (index % 4 == 0) array[index] += margin.x
                else if (index % 4 == 1) array[index] += margin.y
            }
        }
    }

    override fun build(): VertexArray {
        adjustBorder()
        return super.build()
    }

    override fun adjustBorder() {
        adjustAction(array, sizeRate, margin)
    }
}