import {AxiosBuilderCreator} from "./AxiosHttp";


export async function CheckVersion():Promise<void> {
  const baseUrl = window.location.origin
  const builder = AxiosBuilderCreator()(baseUrl)
  builder.setUri("/static/version")
  const [version, err] = await builder.build().send()
  if (err!=null) {
    console.error("CheckVersion err", err)
    return ;
  }

  const key = CheckVersion.name
  const oldVersion = localStorage.getItem(key)
  if (oldVersion === version) {
    return ;
  }
  localStorage.setItem(key, version)

  if (!oldVersion) {
    return ;
  }

  window.location.reload()
}
