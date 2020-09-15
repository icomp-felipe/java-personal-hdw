package com.hdw.testing;

import com.github.kokorin.jaffree.ffmpeg.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

public class ReEncode {
    private final Path ffmpegBin;
    private final Path input;
    private final Path output;

    public ReEncode(Path ffmpegBin, Path input, Path output) {
        this.ffmpegBin = ffmpegBin;
        this.input = input;
        this.output = output;
    }

    public void execute() {
        // The most reliable way to get video duration
        // ffprobe for some formats can't detect duration
    	
    	// ffmpeg -i "$CHUNKLIST" -c copy -bsf:a aac_adtstoasc "$OUTPUT.mp4"
        final AtomicLong duration = new AtomicLong();
        final FFmpegResult nullResult = FFmpeg.atPath(ffmpegBin)
                .addInput(UrlInput.fromPath(input))
                .addOutput(new NullOutput())
                .setOverwriteOutput(true)
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgress(FFmpegProgress progress) {
                        duration.set(progress.getTimeMillis());
                    }
                })
                .execute();
        

        ProgressListener listener = new ProgressListener() {
            private long lastReportTs = System.currentTimeMillis();

            @Override
            public void onProgress(FFmpegProgress progress) {
                long now = System.currentTimeMillis();
                if (lastReportTs + 1000 < now) {
                    long percent = 100 * progress.getTimeMillis() / duration.get();
                    System.out.println("Progress: " + percent + "%");
                }
            }
        };

        FFmpegResult result = FFmpeg.atPath(ffmpegBin)
                .addInput(UrlInput.fromPath(input))
                .addArguments("-bsf:a", "aac_adtstoasc")
                .addOutput(UrlOutput.toPath(output))
                .setProgressListener(listener)
                .setOverwriteOutput(true)
                .execute();
    }

    public static void main(String[] args) throws Exception {

    	double a = 500.566f;
    	int b = (int) a;
    	
    	System.out.println(b);
    	
    	/*System.out.println();
    	
        new ReEncode(
                Paths.get("/usr/bin/"),
                Paths.get("https://svbp-sambavideos.akamaized.net/voda/_definst_/amlst%3Astg%3B2739%2C10004%2C061c08132eb6c705eeefca3fd08434fe%3Bhidden32np%3B3WUIQJCUWYAH2AWAHJDMXO3Y52VALOOXHBS5LWD3KTRYEEDF4WQLA5CJ4GT2YVABCOFSSKLKLDSIGNH2MAQEEHPK5SL4CVL7NYNWWLNGZKLWZ2N2ZP6FRKI5IJ6SK75ZA37SWOML6WUSPMT7NUANU76BLXLSX334JR3NGHPEY3DNDXQQKQZRKYREVRSXQPSYMLT72UC5J3RVVP6LC3ZFDPFFDNFMXXIUCPR24MXRVFXMC2URFEL2EX22NZDZBTG6453TAS6NNFWEEXORCVUVDCZEKED3AVTS35HJ7RPICYE2TGR4JXS7ZRIB4CFPTUD7GFYRFOXNNJ7C3IQPPUAWULZOBGCA6VJ7IWJM2OQOHGFHEZQ2TU%21%21%21%21%21%21/chunklist_b1288192.m3u8"),
                Paths.get("/tmp/c.avi")
        ).execute();*/
    }
}
