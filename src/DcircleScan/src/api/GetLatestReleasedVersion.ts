import {Json} from "../3rdparty/ts-json";
import {PostJsonNoToken} from "../3rdparty/ts-baselib";
import {getUs} from "../DIDBrowser";

class GetLatestReleasedVersionRequest {
  // public channel = AppChannel
  public channel = 'apk'
  public versionCode: number = 0
  public appVersion:string = '0'
}

export enum UpdateCode {
  NoUpdate = 0,
  CanUpdate = 1,
  ForceUpdate = 2
}

class ReleasedVersionInfo {
  public appVersion:string = ""
  public versionCode:number = 0
  public downLoadUrl:string = ""
  public channel:string = ""
  public describe:string = ""
  public apkHash:string = ""
}

export class Data {
  public updateCode: number = 0
  public latestVersion: ReleasedVersionInfo = new ReleasedVersionInfo()
}

export class GetLatestReleasedVersionResponse {
  public data:Data = new Data()
}

export async function GetLatestReleasedVersion():Promise<[GetLatestReleasedVersionResponse, Error|null]> {
  const net = getUs().nf.get();
  const [ret, err] = await PostJsonNoToken("/im/user/GetLatestReleasedVersion", {data: new GetLatestReleasedVersionRequest()}, GetLatestReleasedVersionResponse, net);
  if (err) {
    return [new GetLatestReleasedVersionResponse(), err];
  }
  if (ret.data.latestVersion.downLoadUrl.length <= 0) {
    const res = localStorage.getItem('appVersionInfo')
    if (!res) {
      return [new GetLatestReleasedVersionResponse(), err];
    }
    return new Json().fromJson(res, GetLatestReleasedVersionResponse)
  }
  localStorage.setItem('appVersionInfo', new Json().toJson(ret))
  return [ret, null];
}
