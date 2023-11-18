# Bluetooth Flow Project

**Last Edited At:** 11/18/2023
**Editors:** Ali Mehdizadeh

# Table of Contents

* [Introduction](#sec-introduction)
* [Requirements](#sec-requirements)
* [Project Hierarchy](#sec-projectHierarchy)
* [User Interface](#sec-ui)
* [Util](#sec-util)

___
<a id="sec-introduction"></a>

## Introduction

Android Bluetooth classic API wrapped in Coroutines / Flow inspired by RxBluetooth and
AndroidBluetoothLibrary.
This library can handle ble type and classic type bluetooth.

___
<a id="sec-requirements"></a>

## Requirements

|Id|Title|Description|
|--|-----|-----------|
|1 | Structural Architecture| MVVM && CLEAN ARCHITECTURE
|2 | Technologies | AndroidX,Coroutine,LiveData,DataBinding,Flow,
|3 | Theme and Components | Material
|4 | Development Language | Kotlin

___
<a id="sec-projectHierarchy"></a>

## Project Hierarchy

* app
    * debug
        * res
            * values
    * main
        * java
            * com.bluetooth.bluetoothflow
                * domain
                   * useCase
                * bluetoothflow
                * presentation
                    * activity
                    * base
                    * fragment
                * utils
                    * permission
        * res
            * drawable
            * layout
            * mipmap
            * navigation
            * values
                * themes
            * xml

    * release
        * res
            * values

___


<a id="sec-di"></a>


## User Interface:

There are **BaseActivity,BaseFragment,BaseViewModel** classes in **base** package that all
activities and fragments and viewModels should extends from those

**Activities** and **Fragments** declare in **ui** package in specific package . for each activity
and fragment you it has viewModel

**MyBinding Adapter** provides adapter for common list view. for each list declare **ViewHolder**
class in **viewHolder** package,**ItemViewModel** class in **vm** package and a resource in *
*res/layout** package

___
<a id="sec-util"></a>

## Util:

There are many **Helper** class and **CustomView** class in **util** package, it also contains some
useful classes.
___
<a id="sec-util"></a>
## Bluetooth Flow
There are bluetooth flow and bluetooth flow IO that search and connect bluetooth device which you can send and read string and byte


