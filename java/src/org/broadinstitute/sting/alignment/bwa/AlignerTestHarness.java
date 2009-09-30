package org.broadinstitute.sting.alignment.bwa;

import org.broadinstitute.sting.alignment.bwa.bwt.*;
import org.broadinstitute.sting.alignment.Aligner;
import org.broadinstitute.sting.alignment.Alignment;
import org.broadinstitute.sting.utils.StingException;
import org.broadinstitute.sting.utils.BaseUtils;
import org.broadinstitute.sting.utils.fasta.IndexedFastaSequenceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileReader;

/**
 * A test harness to ensure that the perfect aligner works.
 *
 * @author mhanna
 * @version 0.1
 */
public class AlignerTestHarness {
    public static void main( String argv[] ) throws FileNotFoundException {
        if( argv.length != 6 ) {
            System.out.println("PerfectAlignerTestHarness <fasta> <bwt> <rbwt> <sa> <rsa> <bam>");
            System.exit(1);
        }

        File referenceFile = new File(argv[0]);
        File bwtFile = new File(argv[1]);
        File rbwtFile = new File(argv[2]);
        File suffixArrayFile = new File(argv[3]);
        File reverseSuffixArrayFile = new File(argv[4]);
        File bamFile = new File(argv[5]);

        align(referenceFile,bwtFile,rbwtFile,suffixArrayFile,reverseSuffixArrayFile,bamFile);
    }

    private static void align(File referenceFile, File bwtFile, File rbwtFile, File suffixArrayFile, File reverseSuffixArrayFile, File bamFile) throws FileNotFoundException {
        Aligner aligner = new BWAAligner(bwtFile,rbwtFile,suffixArrayFile,reverseSuffixArrayFile);
        int count = 0;

        SAMFileReader reader = new SAMFileReader(bamFile);
        reader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);

        for(SAMRecord read: reader) {
            count++;
            //if( count > 25000 ) break;
            //if( count != 39 ) continue;
            //if( !read.getReadName().endsWith("1507:1636#0") )
            //    continue;

            SAMRecord alignmentCleaned = null;
            try {
                alignmentCleaned = (SAMRecord)read.clone();
            }
            catch( CloneNotSupportedException ex ) {
                throw new StingException("SAMRecord clone not supported", ex);
            }

            if( alignmentCleaned.getReadNegativeStrandFlag() )
                alignmentCleaned.setReadBases(BaseUtils.simpleReverseComplement(alignmentCleaned.getReadBases()));

            alignmentCleaned.setReferenceIndex(SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX);
            alignmentCleaned.setAlignmentStart(SAMRecord.NO_ALIGNMENT_START);
            alignmentCleaned.setMappingQuality(SAMRecord.NO_MAPPING_QUALITY);
            alignmentCleaned.setCigarString(SAMRecord.NO_ALIGNMENT_CIGAR);

            // Clear everything except flags pertaining to pairing and set 'unmapped' status to true.
            alignmentCleaned.setFlags(alignmentCleaned.getFlags() & 0x00A1 | 0x000C);

            List<Alignment> alignments = aligner.align(alignmentCleaned);
            if(alignments.size() == 0 )
                throw new StingException(String.format("Unable to align read %s to reference; count = %d",read.getReadName(),count));

            Alignment alignment = alignments.get(0);

            System.out.printf("%s: Aligned read to reference at position %d with %d mismatches, %d gap opens, and %d gap extensions.%n", read.getReadName(), alignment.getAlignmentStart(), alignment.getMismatches(), alignment.getGapOpens(), alignment.getGapExtensions());

            if( read.getReadNegativeStrandFlag() != alignment.isNegativeStrand() )
                throw new StingException("Read has been aligned in wrong direction");

            if( read.getAlignmentStart() != alignment.getAlignmentStart() ) {
                IndexedFastaSequenceFile reference = new IndexedFastaSequenceFile(referenceFile);
                String expectedRef = new String(reference.getSubsequenceAt(reference.getSequenceDictionary().getSequences().get(0).getSequenceName(),read.getAlignmentStart(),read.getAlignmentStart()+read.getReadLength()-1).getBases());
                int expectedMismatches = 0;
                for( int i = 0; i < read.getReadLength(); i++ ) {
                    if( read.getReadBases()[i] != expectedRef.charAt(i) )
                        expectedMismatches++;
                }

                String alignedRef = new String(reference.getSubsequenceAt(reference.getSequenceDictionary().getSequences().get(0).getSequenceName(),alignments.get(0).getAlignmentStart(),alignments.get(0).getAlignmentStart()+read.getReadLength()-1).getBases());
                int actualMismatches = 0;
                for( int i = 0; i < read.getReadLength(); i++ ) {
                    if( read.getReadBases()[i] != alignedRef.charAt(i) )
                        actualMismatches++;
                }

                if( expectedMismatches != actualMismatches ) {
                    System.out.printf("read          = %s%n", read.getReadString());
                    System.out.printf("expected ref  = %s%n", expectedRef);
                    System.out.printf("actual ref    = %s%n", alignedRef);
                    throw new StingException(String.format("Read %s was placed at incorrect location; target alignment = %d; actual alignment = %d%n",read.getReadName(),read.getAlignmentStart(),alignment.getAlignmentStart()));
                }
            }

            if( count % 1000 == 0 )
                System.out.printf("%d reads examined.%n",count);                
        }

        System.out.printf("%d reads examined.%n",count);
    }

}
