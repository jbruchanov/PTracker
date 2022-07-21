package com.scurab.ptracker.app.model

import com.scurab.ptracker.ui.DateTimeFormats

interface IDataTransformers {
    val dateTimeFormats: DateTimeFormats
}

class DataTransformers(
    override val dateTimeFormats: DateTimeFormats = DateTimeFormats
) : IDataTransformers
