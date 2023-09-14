import {Http, HttpBuilder} from "../3rdparty/ts-baselib";
import axios from "axios";

class AxiosHttp implements Http {
  constructor(private builder: HttpBuilder) {}

  async send(): Promise<[string, (Error | null)]> {
    try {
      const response = await axios.get(`${this.builder.baseUrl()}${this.builder.uri()}`)
      if (response.status === 200) {
        return [response.data as string, null]
      }

      return ["", Error(`code is ${response.status}`)]
    } catch (e) {
      return ["", Error("failed")]
    }
  }
}

export function AxiosBuilderCreator(): (baseUrl:string)=>HttpBuilder {
  return baseUrl => {
    return new class extends HttpBuilder {
      build(): Http {
        return new AxiosHttp(this)
      }
    }(baseUrl)
  }
}
