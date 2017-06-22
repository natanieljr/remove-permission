package org.droidmate.analyzer.wrappers

import org.droidmate.analyzer.exploration.IExplorationResult
import java.nio.file.Path

/**
 * BoxMate wrapper interface
 */
interface IBoxMateWrapper{
    fun inlineApp(apk: Path): Path
    fun explore(apk: Path, policiesFile: Path, isInitialExpl: Boolean): IExplorationResult
}
