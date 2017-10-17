/*
* Copyright 2012-2016 Broad Institute, Inc.
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

package org.broadinstitute.gatk.tools.walkers.coverage;

import org.broadinstitute.gatk.engine.walkers.WalkerTest;
import org.testng.annotations.Test;
import org.testng.Assert;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration tests for the Depth of Coverage walker
 *
 * @Author chartl
 * @Date Feb 25, 2010
 */
public class DepthOfCoverageIntegrationTest extends WalkerTest {

    private boolean RUN_TESTS = true;
    private String root = "-T DepthOfCoverage ";

    private String buildRootCmd(String ref, List<String> bams, List<String> intervals) {
        StringBuilder bamBuilder = new StringBuilder();
        do {
            bamBuilder.append(" -I ");
            bamBuilder.append(bams.remove(0));
        } while ( bams.size() > 0 );

        StringBuilder intervalBuilder = new StringBuilder();
        do {
            intervalBuilder.append(" -L ");
            intervalBuilder.append(intervals.remove(0));
        } while ( intervals.size() > 0 );


        return root + "-R "+ref+bamBuilder.toString()+intervalBuilder.toString();
    }

    private void execute(String name, WalkerTestSpec spec) {
        if ( RUN_TESTS ) {
            executeTest(name,spec);
        }
    }

    @Test
    public void testBaseOutputNoFiltering() {
        final String[] intervals = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/fhs_jhs_30_targts.interval_list"};
        final String[] bams = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/FHS_indexed_subset.bam"};

        final String cmd = buildRootCmd(hg18Reference,new ArrayList<>(Arrays.asList(bams)),new ArrayList<>(Arrays.asList(intervals))) + " -mmq 0 -mbq 0 -dels -baseCounts -pt readgroup -pt sample -pt library --outputFormat csv -ct 10 -ct 15 -ct 20 -ct 25";
        final WalkerTestSpec spec = new WalkerTestSpec(cmd,0, new ArrayList<String>());

        // our base file
        final File baseOutputFile = createTempFile("depthofcoveragenofiltering",".tmp");
        spec.setOutputFileLocation(baseOutputFile);

        // now add the expected files that get generated
        spec.addAuxFile("423571e4c05e7934322172654ac6dbb7", baseOutputFile);
        spec.addAuxFile("9df5e7e07efeb34926c94a724714c219", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_cumulative_coverage_counts"));
        spec.addAuxFile("3bf1d7247ea68d1afb35c2032c68dbdf", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_cumulative_coverage_proportions"));
        spec.addAuxFile("9cd395f47b329b9dd00ad024fcac9929", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_interval_statistics"));
        spec.addAuxFile("31f9aa7ddad7809bc2f6675014dd4eaf", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_interval_summary"));
        spec.addAuxFile("958667ce6f92106a32a77098c1533d4f", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_statistics"));
        spec.addAuxFile("5e87db30702e44031920f7417cfac844", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_summary"));
        spec.addAuxFile("b82846df660f0aac8429aec57c2a62d6", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_cumulative_coverage_counts"));
        spec.addAuxFile("22fee04ffca7fa489ae2e66309de12e4", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_cumulative_coverage_proportions"));
        spec.addAuxFile("7b9d0e93bf5b5313995be7010ef1f528", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_interval_statistics"));
        spec.addAuxFile("e0f0d44cb4bc0557ee55b8b2aaa0cb9f", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_interval_summary"));
        spec.addAuxFile("d69d08fd37f63941eea52d7b66a625df", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_statistics"));
        spec.addAuxFile("de1795f42397d1282c012a3b9c396b5c", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".read_group_summary"));
        spec.addAuxFile("d53431022f7387fe9ac47814ab1fcd88", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_counts"));
        spec.addAuxFile("f6e52c5659f53afdcfc69c25272b9e99", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_proportions"));
        spec.addAuxFile("df0ba76e0e6082c0d29fcfd68efc6b77", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_statistics"));
        spec.addAuxFile("a72253b8e06dc6c653355b20e56e2f6e", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_summary"));
        spec.addAuxFile("23ca7c647e3e53eb9b713d46b92bf6a2", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_statistics"));
        spec.addAuxFile("cd4ef2d43dc98f1bd7e6db532c60b3d1", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_summary"));

        execute("testBaseOutputNoFiltering",spec);
    }

    @Test
    public void testNoCoverageDueToFiltering() {
        String[] intervals = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/fhs_jhs_30_targts.interval_list"};
        String[] bams = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/FHS_indexed_subset.bam"};

        String cmd = buildRootCmd(hg18Reference,new ArrayList<String>(Arrays.asList(bams)),new ArrayList<String>(Arrays.asList(intervals))) + " -mmq 0 -mbq 5 --maxBaseQuality 4 -dels -baseCounts -pt readgroup -pt sample -pt library --outputFormat csv";
        WalkerTestSpec spec = new WalkerTestSpec(cmd,0, new ArrayList<String>());

        File baseOutputFile = createTempFile("depthofcoveragenofiltering",".tmp");
        spec.setOutputFileLocation(baseOutputFile);

        spec.addAuxFile("6ccd7d8970ba98cb95fe41636a070c1c",baseOutputFile);
        spec.addAuxFile("fcaac186b7ccd10f3a6cb89ea49dcc1e",createTempFileFromBase(baseOutputFile.getAbsolutePath()+".library_interval_summary"));

        execute("testNoCoverageDueToFiltering",spec);
    }

    @Test
    public void testAdjacentIntervals() {
        String[] intervals = {"chr1:1-999", "chr1:1000-65536", "chr1:65537-80000", "chr1:80001-81000"};
        String[] bams = {publicTestDir+"exampleBAM.bam"};

        String cmd = buildRootCmd(exampleFASTA, new ArrayList<String>(Arrays.asList(bams)), new ArrayList<String>(Arrays.asList(intervals))) + " -im OVERLAPPING_ONLY";
        WalkerTestSpec spec = new WalkerTestSpec(cmd, 0, new ArrayList<String>());

        File baseOutputFile = WalkerTest.createTempFile("depthofcoverageadjinterval", ".tmp");
        spec.setOutputFileLocation(baseOutputFile);

        spec.addAuxFile("84b95d62f53e28919d1b5286558a1cae", baseOutputFile);
        spec.addAuxFile("e445d4529dd3e3caa486ab8f5ec63e49", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_counts"));
        spec.addAuxFile("b69c89ba8b0c393b735616c2bc3aea76", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_proportions"));
        spec.addAuxFile("788988dac6119a02de2c8d4dfb06b727", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_statistics"));
        spec.addAuxFile("3769ed40ab3ccd2ed94a9dc05cc2bc2f", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_summary"));
        spec.addAuxFile("1281605e022d7462fbbcd14de53d1ca3", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_statistics"));
        spec.addAuxFile("4b41d6ff88aa2662697cb7e4b5346cb8", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_summary"));

        execute("testAdjacentIntervals", spec);
    }


    @Test
    public void testSortOrder() {
        // This test came from a user who discovered that the columns and data in the gene_summary file didn't align for the specific
        // sample names in these files.
        String[] intervals = {"1:1600000-1700000"};
        String[] bams = {privateTestDir+"badHashName1.bam", privateTestDir+"badHashName2.bam"};

        String cmd = buildRootCmd(b37KGReference, new ArrayList<String>(Arrays.asList(bams)), new ArrayList<String>(Arrays.asList(intervals))) +
                " -geneList "+privateTestDir+"refGene_CDK11B.txt";
        WalkerTestSpec spec = new WalkerTestSpec(cmd, 0, new ArrayList<String>());

        File baseOutputFile = WalkerTest.createTempFile("depthofcoveragesortorder", ".tmp");
        spec.setOutputFileLocation(baseOutputFile);

        spec.addAuxFile("a148e50f9db207adfd5d5f0f29eb54d8", baseOutputFile);
        spec.addAuxFile("7ccd5193a3c035d1cc856cbc89e3daf4", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_counts"));
        spec.addAuxFile("2efe59c20721ce61bc5b334a26d11720", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_proportions"));
        spec.addAuxFile("3dd61151e758c1f3c4bd1150b6577b45", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_gene_summary"));
        spec.addAuxFile("cf62d95ec1f459fbbe35370c3f0ca481", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_statistics"));
        spec.addAuxFile("b4fcb739b7f9e309e38a7d5e7e4ebb9f", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_summary"));
        spec.addAuxFile("6bf63f9c62071e850c6f0b6356fb63eb", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_statistics"));
        spec.addAuxFile("e53e6a494bf1cf817762b74917c6f0c9", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_summary"));

        execute("testSortOrder", spec);
    }

    public void testRefNHandling(boolean includeNs, final String md5) {
        String command = "-R " + b37KGReference + " -L 20:26,319,565-26,319,575 -I " + validationDataLocation + "NA12878.HiSeq.WGS.bwa.cleaned.recal.hg19.20.bam -T DepthOfCoverage -baseCounts --omitIntervalStatistics --omitLocusTable --omitPerSampleStats -o %s";
        if ( includeNs ) command += " --includeRefNSites";
        WalkerTestSpec spec = new WalkerTestSpec(command, 1, Arrays.asList(md5));
        executeTest("Testing DoC " + (includeNs ? "with" : "without") + " reference Ns", spec);
    }

    @Test public void testRefNWithNs() { testRefNHandling(true, "24cd2da2e4323ce6fd76217ba6dc2834"); }
    @Test public void testRefNWithoutNs() { testRefNHandling(false, "4fc0f1a2e968f777d693abcefd4fb7af"); }


    @Test
    public void testIncompatibleArgs() throws IOException {
        final String[] intervals = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/fhs_jhs_30_targts.interval_list"};
        final String[] bams = {"/humgen/gsa-hpprojects/GATK/data/Validation_Data/FHS_indexed_subset.bam"};
        final String refSeqGeneListFile = privateTestDir + "geneTrackHg18Chr1Interval.refSeq";

        final String logFileName = new String("testIncompatibleArgs.log");
        final String cmd = buildRootCmd(hg18Reference,new ArrayList<>(Arrays.asList(bams)),new ArrayList<>(Arrays.asList(intervals))) + " --omitIntervalStatistics --calculateCoverageOverGenes " + refSeqGeneListFile + " -log " + logFileName;
        final WalkerTestSpec spec = new WalkerTestSpec(cmd,0, new ArrayList<String>());

        // output file
        final File outputFile = createTempFile("DepthOfCoverageIncompatibleArgs",".tmp");
        spec.setOutputFileLocation(outputFile);

        execute("testIncompatibleArgs",spec);

        // check that only the sample gene summary output file is empty
        Assert.assertEquals( createTempFileFromBase(outputFile.getAbsolutePath()+".sample_gene_summary").length(), 0 );
        Assert.assertNotEquals( createTempFileFromBase(outputFile.getAbsolutePath()+".sample_cumulative_coverage_counts").length(), 0 );
        Assert.assertNotEquals( createTempFileFromBase(outputFile.getAbsolutePath()+".sample_cumulative_coverage_proportions").length(), 0 );
        Assert.assertNotEquals( createTempFileFromBase(outputFile.getAbsolutePath()+".sample_statistics").length(), 0 );
        Assert.assertNotEquals( createTempFileFromBase(outputFile.getAbsolutePath()+".sample_summary").length(), 0 );

        // check the log for the warning message
        File file = new File(logFileName);
        Assert.assertTrue(FileUtils.readFileToString(file).contains(DepthOfCoverage.incompatibleArgsMsg()));
    }

    @Test
    public void testSampleGeneSummaryHeaderFix(){
        final String[] intervals = {privateTestDir+"intervals.list"};
        final String[] bams = {privateTestDir+"U145055CM2013D16882_snippet.bam", privateTestDir+"U145055PF2013D20189_snippet.bam", privateTestDir+"ControleMan1_snippet.bam"};
        String cmd = buildRootCmd(b37KGReference, new ArrayList<String>(Arrays.asList(bams)), new ArrayList<String>(Arrays.asList(intervals))) +
                " -geneList "+privateTestDir+"ENSG00000186092.rod";

        WalkerTestSpec spec = new WalkerTestSpec(cmd, 0, new ArrayList<String>());

        File baseOutputFile = WalkerTest.createTempFile("depthOfCoverageSampleGeneSummaryHeaderFix", ".tmp");
        spec.setOutputFileLocation(baseOutputFile);

        spec.addAuxFile("0b698e9d6c28479c378bf64182100e11", baseOutputFile);
        spec.addAuxFile("97a782ce07fc487d0945b60f66721048", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_counts"));
        spec.addAuxFile("02cd919ef71d8bcfb1c1622b7a683033", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_cumulative_coverage_proportions"));
        spec.addAuxFile("799d8ff6a12d3109f985953ce91ab17c", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_gene_summary"));
        spec.addAuxFile("c9624eaa0a800da0b457daa752f7ed92", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_statistics"));
        spec.addAuxFile("34ac6759bff2d34890a30a44f8887336", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_interval_summary"));
        spec.addAuxFile("166b97db63a95c3ceb44d790d98a6fc1", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_statistics"));
        spec.addAuxFile("77e03df80132408c6c773cdb1f835e83", createTempFileFromBase(baseOutputFile.getAbsolutePath()+".sample_summary"));

        execute("testSampleGeneSummaryHeaderFix", spec);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMissingSAMHeaderReadGroup() {
        final String[] intervals = {"chr1:200-300"};
        final String[] bams = {privateTestDir + "exampleBAMNoRG.bam"};

        final String cmd = buildRootCmd(exampleFASTA,new ArrayList<>(Arrays.asList(bams)),new ArrayList<>(Arrays.asList(intervals)));
        final WalkerTestSpec spec = new WalkerTestSpec(cmd, 0, new ArrayList<String>());
        execute("testMissingSAMHeaderReadGroup", spec);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMissingSAMHeaderReadGroupSample() {
        final String[] intervals = {"chr1:200-300"};
        final String[] bams = {privateTestDir + "exampleBAMNoSM.bam"};

        final String cmd = buildRootCmd(exampleFASTA,new ArrayList<>(Arrays.asList(bams)),new ArrayList<>(Arrays.asList(intervals)));
        final WalkerTestSpec spec = new WalkerTestSpec(cmd, 0, new ArrayList<String>());
        execute("testMissingSAMHeaderReadGroupSample", spec);
    }
}
