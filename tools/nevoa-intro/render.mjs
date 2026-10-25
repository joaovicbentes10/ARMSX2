// Renders intro.html frame by frame and encodes out/boot_intro.mp4.
// Usage: node render.mjs [--crf 20] [--audio-from existing.mp4] [--still 1.5,4.2,...]   (--still writes out/still-<t>.png and skips the video)
import puppeteer from 'puppeteer';
import { mkdirSync, mkdtempSync, rmSync, statSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { execFileSync } from 'node:child_process';

const here = dirname(fileURLToPath(import.meta.url));
const outDir = join(here, 'out');
const arg = name => { const i = process.argv.indexOf(name); return i > 0 ? process.argv[i + 1] : undefined; };
const crf = arg('--crf') ?? '20';
const stills = arg('--still')?.split(',').map(Number);
// Timing-only edits can preserve the installed soundtrack byte for byte.
const audioFrom = arg('--audio-from');
const audioInput = audioFrom ?? join(here, '../../platforms/android/app/src/main/res/raw/nevoa_intro_sound.mp3');
const audioCodec = audioFrom ? ['-c:a', 'copy'] : ['-c:a', 'aac', '-b:a', '192k'];
mkdirSync(outDir, { recursive: true });

// SwiftShader keeps the output identical across machines/GPUs.
const browser = await puppeteer.launch({ args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] });
const tmp = mkdtempSync(join(tmpdir(), 'nevoa-intro-'));
try {
  const page = await browser.newPage();
  page.on('pageerror', e => { throw e; });
  await page.setViewport({ width: 2400, height: 1080, deviceScaleFactor: 1 });
  await page.goto(pathToFileURL(join(here, 'intro.html')).href, { waitUntil: 'load' });
  await page.evaluate(() => document.fonts.ready);

  const check = await page.evaluate(() => window.selfCheck());
  console.log('last frame vs NevoaShader(iTime=0):', check);
  if (check.maxDiff > 3 || check.wordmarkOpacity !== 0) throw new Error('last frame does not match the library background');

  const shoot = async (t, path) => {
    // Two rAFs so the compositor has presented this frame before the screenshot.
    await page.evaluate(t => { window.renderFrame(t); return new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r))); }, t);
    await page.screenshot({ path, type: 'png' });
  };

  if (stills) {
    for (const t of stills) await shoot(t, join(outDir, `still-${t}.png`));
  } else {
    const frames = await page.evaluate(() => window.FRAMES), fps = await page.evaluate(() => window.FPS);
    for (let i = 0; i < frames; i++) {
      await shoot(i / fps, join(tmp, `${String(i).padStart(4, '0')}.png`));
      if (i % 30 === 0) console.log(`frame ${i}/${frames}`);
    }
    const mp4 = join(outDir, 'boot_intro.mp4');
    execFileSync('ffmpeg', ['-y', '-v', 'error', '-framerate', String(fps), '-i', join(tmp, '%04d.png'),
      '-i', audioInput,
      '-map', '0:v:0', '-map', '1:a:0', ...audioCodec, ...(audioFrom ? [] : ['-shortest']),
      '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', crf, '-movflags', '+faststart', mp4], { stdio: 'inherit' });
    console.log(`${mp4}: ${(statSync(mp4).size / 1048576).toFixed(2)} MB`);
    // Main's window background: the app shows this exact frame while it starts, so the intro hands off without a cut.
    await shoot((frames - 1) / fps, join(outDir, 'last_frame.png'));
    const webp = join(outDir, 'nevoa_last_frame.webp');
    execFileSync('ffmpeg', ['-y', '-v', 'error', '-i', join(outDir, 'last_frame.png'), '-quality', '90', webp], { stdio: 'inherit' });
    console.log(`${webp}: ${(statSync(webp).size / 1024).toFixed(0)} KB`);
  }
} finally {
  await browser.close();
  rmSync(tmp, { recursive: true, force: true });
}
