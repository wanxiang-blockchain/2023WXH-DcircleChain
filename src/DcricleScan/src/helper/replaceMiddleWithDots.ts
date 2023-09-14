export default function replaceMiddleWithDots(did: string) {
  if(!did) {
    return
  }
  const middleStart = Math.floor(did.length / 2) - 4;
  const middleEnd = Math.ceil(did.length / 2) + 4;
  const regex = new RegExp(`(.{${middleStart}}).{8}(.{${did.length - middleEnd}})`);
  return did.replace(regex, `$1...$2`);
}
