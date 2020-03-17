package ru.bitec.remotebloop.rbpcommander

import java.nio.file.Path

import bloop.config.Config.Project
import xsbti.compile.AnalysisContents

case class LocalProject(
                         project: Project,
                         analysisContentsOpt: Option[AnalysisContents]=None,
                         remoteCacheOpt: Option[Path]=None)

case class RemoteProject(project: Project, analysisContentsOpt: Option[AnalysisContents])

