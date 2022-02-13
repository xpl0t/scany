package com.xpl0t.scany.ui.scanimage

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class ScanBitmaps(
    val source: Bitmap,
    val crop: Bitmap,
    val improved: Bitmap
) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(source, 0)
        parcel.writeParcelable(crop, 0)
        parcel.writeParcelable(improved, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScanBitmaps> {
        override fun createFromParcel(parcel: Parcel): ScanBitmaps {
            return ScanBitmaps(
                parcel.readParcelable(Bitmap::class.java.classLoader)!!,
                parcel.readParcelable(Bitmap::class.java.classLoader)!!,
                parcel.readParcelable(Bitmap::class.java.classLoader)!!
            )
        }

        override fun newArray(size: Int): Array<ScanBitmaps?> {
            return arrayOfNulls(size)
        }
    }
}