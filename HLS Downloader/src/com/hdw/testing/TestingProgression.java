package com.hdw.testing;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.phill.libs.PhillFileUtils;

import net.bramp.ffmpeg.*;
import net.bramp.ffmpeg.builder.*;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.*;

public class TestingProgression {

    public static void main(String[] args) throws IOException {

        FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegProbeResult in = ffprobe.probe("http://us-cplus-aka.canal-plus.com/i/1607/14/1173352_140_,200k,400k,800k,1500k,.mp4.csmil/index_3_av.m3u8");
        
        FFmpegFormat i = in.getFormat();
        System.out.println(i.bit_rate);
        System.out.println(FFmpegUtils.toTimecode((long) i.duration, TimeUnit.SECONDS));
        System.out.println(i.filename);
        System.out.println(i.format_long_name);
        System.out.println(i.format_name);
        System.out.println(i.nb_programs);
        System.out.println(i.nb_streams);
        System.out.println(i.probe_score);
        System.out.println(i.size);
        System.out.println(i.start_time);
        
       FFmpegBuilder builder = new FFmpegBuilder()
                .addInput("http://us-cplus-aka.canal-plus.com/i/1607/14/1173352_140_,200k,400k,800k,1500k,.mp4.csmil/index_3_av.m3u8")
                .addOutput("/tmp/foo.mp4")
                    .setAudioCodec("copy")
                    .setVideoCodec("copy")
                    .setAudioBitStreamFilter("aac_adtstoasc")
                .done();

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {

        	// Using the FFmpegProbeResult determine the duration of the input
        	final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

        	@Override
        	public void progress(Progress progress) {
        		
        		
        		double percentage = progress.out_time_ns / duration_ns;

        		System.out.printf("[%.0f%%] frame: %d | time: %s | fps: %.0f | speed: %.2fx | bytes loaded: %s\n | status: %s",
      				  percentage * 100,
  					  progress.frame,
  					  FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
  					  progress.fps.doubleValue(),
  					  progress.speed,
  					  PhillFileUtils.humanReadableByteCount(progress.total_size),progress.status
  					  );
        		
        		// Print out interesting information about the progress
        		/*System.out.println(String.format(
        			"[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
        			percentage * 100,
        			progress.status,
        			progress.frame,
        			FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
        			progress.fps.doubleValue(),
        			progress.speed
        		));*/
        	}
        });

        job.run();
    }
}