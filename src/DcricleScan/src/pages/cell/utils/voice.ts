const voiceMap = new Map<string, Function>([])

export function setVoiceControlPlayOrPause(msgId: string, cb:Function) {
  if (voiceMap.has(msgId)) {
    return;
  }
  voiceMap.set(msgId, cb)
  return;
}
export async function resetVoices(msgId:string) {
  return new Promise(resolve => {
    if (voiceMap.has(msgId)) {
      const f = voiceMap.delete(msgId);
    }
    const fns = Array.from(voiceMap.values());
    for(let i = 0; i < fns.length; i++) {
      fns[i]();
    }
    resolve(null)
  })
}

