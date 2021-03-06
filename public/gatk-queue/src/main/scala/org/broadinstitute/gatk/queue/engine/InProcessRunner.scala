/*
* Copyright 2012-2015 Broad Institute, Inc.
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.gatk.queue.engine

import org.broadinstitute.gatk.queue.function.InProcessFunction
import java.util.Date
import org.broadinstitute.gatk.utils.Utils
import org.apache.commons.io.{IOUtils, FileUtils}
import java.io.PrintStream

/**
 * Runs a function that executes in process and does not fork out an external process.
 */
class InProcessRunner(val function: InProcessFunction) extends JobRunner[InProcessFunction] {
  private var runStatus: RunnerStatus.Value = _

  def start() {
    getRunInfo.startTime = new Date()
    getRunInfo.exechosts = Utils.resolveHostname()
    runStatus = RunnerStatus.RUNNING

    function.jobOutputStream = new PrintStream(FileUtils.openOutputStream(function.jobOutputFile))
    function.jobErrorStream = {
      if (function.jobErrorFile != null)
        new PrintStream(FileUtils.openOutputStream(function.jobErrorFile))
      else
        function.jobOutputStream
    }
    try {
      function.run()
      function.jobOutputStream.println("%s%nDone.".format(function.description))
    } finally {
      IOUtils.closeQuietly(function.jobOutputStream)
      if (function.jobErrorFile != null)
        IOUtils.closeQuietly(function.jobErrorStream)
    }

    runStatus = RunnerStatus.DONE
    getRunInfo.doneTime = new Date()
  }

  def status = runStatus
}
