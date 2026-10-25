"""Approved Nevoa Crystal V2 game-launch cue; original synthesis, no samples."""
import math, random, wave, struct
from pathlib import Path
RATE=48000
DURATION=0.88
N=round(RATE*DURATION)
root=Path(__file__).resolve().parents[2] / 'platforms/android/app/src/main/res/raw'


def generate():
    rng=random.Random(251025)
    left=[0.0]*N
    right=[0.0]*N
    notes=[(349.2282,0.000,0.70,-0.18),(523.2511,0.012,0.35,0.26),(783.9909,0.027,0.16,-0.28),(698.4565,0.040,0.20,0.14)]
    def smooth(a,b,t):
     x=max(0.,min(1.,(t-a)/(b-a)));return x*x*(3.-2.*x)
    fast=slow=0.
    for i in range(N):
     t=i/RATE
     l=r=0.
     for freq,onset,amp,pan in notes:
      u=t-onset
      if u<0:continue
      p=math.tau*freq*u
      env=(1-math.exp(-u*260))*(0.8*math.exp(-u*6.2)+0.2*math.exp(-u*3.8))
      index=2.3*math.exp(-u*25)
      voice=(0.76*math.sin(p+index*math.sin(p*2.002))+0.16*math.sin(p*3.001)*math.exp(-u*8)+0.08*math.sin(p*4.003)*math.exp(-u*16))*env*amp
      l+=voice*math.sqrt((1-pan)/2)
      r+=voice*math.sqrt((1+pan)/2)
     u=max(0,t-.015)
     padenv=smooth(.015,.11,t)*math.exp(-u*4.8)
     l+=padenv*(.11*math.sin(math.tau*174.6141*u)+.07*math.sin(math.tau*261.6256*u))
     r+=padenv*(.11*math.sin(math.tau*174.6141*1.0012*u)+.07*math.sin(math.tau*261.6256*.9989*u))
     sub=.12*math.sin(math.tau*87.307*u)*(1-math.exp(-u*130))*math.exp(-u*11)
     noise=rng.uniform(-1,1)
     fast+=.32*(noise-fast);slow+=.05*(noise-slow)
     air=.09*(fast-slow)*smooth(0,.04,t)*(1-smooth(.16,.31,t))
     left[i]=l+sub+air
     right[i]=r+sub+air*.87
    # Damped short room: separate reflections surround a clear central transient.
    for channel,times in [(left,[.0237,.0319,.0411]),(right,[.0271,.0367,.0439])]:
     dry=channel[:]
     for delay in times:
      length=round(delay*RATE);buffer=[0.0]*length;damped=0.
      for i in range(N):
       j=i%length;echo=buffer[j]
       damped+=.32*(echo-damped)
       buffer[j]=dry[i]+damped*.56
       channel[i]+=echo*.135
    for i in range(N):
     fade=(1-smooth(.66,DURATION-1/RATE,i/RATE))
     left[i]*=fade;right[i]*=fade
    peak=max(max(abs(x) for x in left),max(abs(x) for x in right))
    scale=.78/peak
    pcm=b''.join(struct.pack('<hh',round(l*scale*32767),round(r*scale*32767)) for l,r in zip(left,right))
    path=root/'nevoa_game_launch.wav'
    with wave.open(str(path),'wb') as w:
     w.setnchannels(2);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(pcm)
    rms=math.sqrt(sum((l*l+r*r)*scale*scale for l,r in zip(left,right))/(2*N))
    print(f'{path}\nduration={DURATION}s peak=0.78 RMS={20*math.log10(rms):.2f}dBFS first={left[0],right[0]} last={left[-1],right[-1]}')


if __name__ == "__main__":
    generate()
