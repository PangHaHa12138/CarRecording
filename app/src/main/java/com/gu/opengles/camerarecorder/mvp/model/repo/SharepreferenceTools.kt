package com.gu.opengles.camerarecorder.mvp.model.repo

import android.content.Context

class SharePreferenceController {
    companion object {
        private const val SHARE_PREFERENCE_NAME = "camera-glsurface-render"
        private const val KEY = "lock_files"
        private const val SPLIT_STR = "_"
        private const val DEFAULT_VALUE = ""
        private fun getLockedVideosHashSet(context: Context): HashSet<String> {
            return read(context).split(SPLIT_STR).filter { it.isNotEmpty() }.toHashSet()
        }

        fun getLockedVideosStr(context: Context): String {
            return read(context)
        }

        fun changeLockedVideos(context: Context, selectedList: List<String>) {
            val set = getLockedVideosHashSet(context)
            selectedList.forEach {
                if (set.contains(it)) {
                    set.remove(it)//如果已经有了，执行清除操作
                } else {
                    set.add(it)//没有，则add操作
                }
            }
            write(context, createValueStr(set))
            set.clear()
        }

        fun removeLockedVideos(context: Context, selectedList: List<String>) {
            val set = getLockedVideosHashSet(context)
            if (set.removeAll(selectedList.toSet())) {
                write(context, createValueStr(set))
            }
            set.clear()
        }


        fun clearAll(context: Context) {
            write(context, DEFAULT_VALUE)
        }

        private fun write(context: Context, value: String) {
            val sp = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
            sp.edit().putString(KEY, value).commit()
        }

        private fun read(context: Context): String {
            val sp = context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
            return sp.getString(KEY, DEFAULT_VALUE)!!
        }

        private fun createValueStr(set: HashSet<String>): String {
            return StringBuilder().apply {
                set.forEach {
                    this.append(it).append(SPLIT_STR)
                }
            }.toString()
        }
    }
}