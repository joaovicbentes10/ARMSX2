"""Original Nevoa game-launch cue. Standard library only; no sampled sounds."""
import math
from pathlib import Path
import random
import struct
import wave

RATE = 48000
DURATION = 0.82  # Leaves 80 ms before the 900 ms home transition hands off.
DESTINATION = Path(__file__).resolve().parents[2] / "platforms/android/app/src/main/res/raw/nevoa_game_launch.wav"


def smooth(start, end, value):
    x = min(1.0, max(0.0, (value - start) / (end - start)))
    return x * x * (3.0 - 2.0 * x)


def generate():
    rng = random.Random(2502026)
    frames = []
    air_fast = air_slow = phase = 0.0
    for index in range(round(RATE * DURATION)):
        t = index / RATE
        noise = rng.uniform(-1.0, 1.0)
        air_fast += 0.32 * (noise - air_fast)
        air_slow += 0.035 * (noise - air_slow)
        sweep = (air_fast - air_slow) * math.sin(math.pi * min(t / 0.46, 1.0)) ** 2
        phase += math.tau * (72.0 + 98.0 * math.exp(-t / 0.16)) / RATE
        body = math.sin(phase) * (1.0 - math.exp(-t * 90.0)) * math.exp(-t * 5.5)
        shimmer = (math.sin(math.tau * 250.0 * t) + 0.35 * math.sin(math.tau * 375.0 * t))
        shimmer *= (1.0 - math.exp(-t * 70.0)) * math.exp(-t * 7.0)
        u = max(0.0, t - 0.42)
        arrival = math.sin(math.tau * 65.0 * u) * (1.0 - math.exp(-u * 100.0)) * math.exp(-u * 14.0)
        fade = 1.0 - smooth(0.62, DURATION - 1.0 / RATE, t)
        pan = -0.22 + 0.44 * min(t / 0.46, 1.0)
        core = 0.28 * body + 0.045 * shimmer + 0.16 * arrival
        left = (core + 0.20 * sweep * (1.0 - pan)) * fade
        right = (core + 0.20 * sweep * (1.0 + pan)) * fade
        frames.append((left, right))
    peak = max(abs(sample) for frame in frames for sample in frame)
    scale = 0.78 / peak
    pcm = b"".join(struct.pack("<hh", round(left * scale * 32767), round(right * scale * 32767)) for left, right in frames)
    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(DESTINATION), "wb") as output:
        output.setnchannels(2)
        output.setsampwidth(2)
        output.setframerate(RATE)
        output.writeframes(pcm)
    print(f"{DESTINATION}: {len(frames) / RATE:.3f} s, stereo PCM16, peak 0.78")


if __name__ == "__main__":
    generate()
