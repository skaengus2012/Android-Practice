package nlab.practice.util.databinding.adapterview

import android.support.annotation.LayoutRes

/**
 * @author Doohyun
 * @since 2018. 07. 11
 */
interface BindAbleItem {
    @LayoutRes fun getLayoutRes() : Int
}