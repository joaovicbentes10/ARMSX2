// Renders intro.html frame by frame and encodes out/boot_intro.mp4.
// Usage: node render.mjs [--crf 20] [--still 1.5,4.2,...]   (--still writes out/still-<t>.png and skips the video)
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
mkdirSync(outDir, { recursive: true });

// SwiftShader keeps the output identical across machines/GPUs.
const browser = await puppeteer.launch({ args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] });
const tmp = mkdtempSync(join(tmpdir(), 'nevoa-intro-'));
try {
  const page = await browser.newPage();
  page.on('pageerror', e => { throw e; });
  await page.setViewport({ width: 1920, height: 1080, deviceScaleFactor: 1 });
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
      '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', crf, '-movflags', '+faststart', '-an', mp4], { stdio: 'inherit' });
    console.log(`${mp4}: ${(statSync(mp4).size / 1048576).toFixed(2)} MB`);
  }
} finally {
  await browser.close();
  rmSync(tmp, { recursive: true, force: true });
}
