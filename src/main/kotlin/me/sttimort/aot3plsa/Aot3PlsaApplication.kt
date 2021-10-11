package me.sttimort.aot3plsa

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import me.sttimort.aot3plsa.commands.PlsaCommand

fun main(args: Array<String>) = NoOpCliktCommand(name = "aot3")
    .subcommands(PlsaCommand())
    .main(args)