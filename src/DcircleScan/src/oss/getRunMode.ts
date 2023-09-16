
export enum RunMode {
  Electron = "Electron",
  Browser = "Browser"
}

export function getRunMode():RunMode {
  return RunMode.Browser;
}
