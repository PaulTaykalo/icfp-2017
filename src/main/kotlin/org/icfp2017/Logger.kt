package org.icfp2017

import java.io.FileWriter

object Logger {
    val file = FileWriter(Arguments.log)

    fun log(command: String) {
        file.write(command)
        file.append('\n')
        file.flush()
    }
}