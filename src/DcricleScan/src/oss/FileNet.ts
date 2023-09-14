import {getLocalFile} from "./LocalFile";
import {GetBucketInfo} from "../api/GetBucketInfo";
import axios from "axios";
import {DBBucket} from "../db/DBBucket";
import {GetDScan} from "../db/db";
import {getUs} from "../DIDBrowser";

class Queue<T> {
  private data_:T[] = [];

  enQueue(data:T) {
    this.data_.push(data);
  }

  data():T[] {
    return this.data_;
  }

  deQueue() {
    if (this.isEmpty()) {
      return null;
    }

    return this.data_.shift();
  }

  isEmpty() {
    return this.data_.length === 0;
  }

  length() {
    return this.data_.length;
  }
}

class FileNet<T> {
  private concurrent_:number = 0;
  protected waitQueue_:Queue<T> = new Queue();

  addRequest(request:T) {
    this.waitQueue_.enQueue(request);
    this._request();
  }

  getMaxConcurrent(){return 10;}

  _request() {
    if (this.concurrent_ >= this.getMaxConcurrent()) {
      return;
    }
    if (this.waitQueue_.isEmpty()) {
      return;
    }

    let data = this.waitQueue_.deQueue();
    this.concurrent_++;
    // @ts-ignore 初步没有排查到为什么编译报错，暂时忽略
    this._execute(data)
      .finally(()=>{
        this.concurrent_--;
        this._request();
      });
  }

  protected async _execute(data:T):Promise<Error|null> {
    console.error("not impl _execute");
    return null;
  }
}

export class Downloader extends FileNet<string> {
  static instance?:Downloader;

  static getInstance() {
    if (!Downloader.instance) {
      Downloader.instance = new Downloader();
    }
    return Downloader.instance;
  }

  addRequest(objectId: string) {
    if (this.waitQueue_.data().indexOf(objectId)>=0) {
      return ;
    }
    super.addRequest(objectId);
  }

  protected async _execute(objectId: string): Promise<Error | null> {
    const db = GetDScan()
    await DBBucket.Insert(db, {id:objectId, progress:0});
    if (await getLocalFile().has(objectId)) {
      await DBBucket.SetProgress(db, objectId, 100);
      await getUs().nc.post(new DBBucket.DownloadProgressEvent([objectId]));
      return null;
    }

    const [bucket, err0] = await GetBucketInfo();
    if (err0) {
      return err0;
    }

    const objectUrl = `https://${bucket.bucketName}.${bucket.endpoint}/${objectId}`

    try {
      const ret = await axios.get(objectUrl, {
        responseType:"arraybuffer",
        onDownloadProgress:async e => {
          await DBBucket.SetProgress(db, objectId, e.progress!*100);
          await getUs().nc.post(new DBBucket.DownloadProgressEvent([objectId]));
        }});

      if (ret.status === 200) {
        await getLocalFile().write(`${objectId}`, new Uint8Array(ret.data))
        await DBBucket.SetProgress(db, objectId, 100);
        await getUs().nc.post(new DBBucket.DownloadProgressEvent([objectId]));
        return null;
      }
      await getUs().nc.post(new DBBucket.DownloadFailEvent([objectId]))
    } catch (e) {
      await getUs().nc.post(new DBBucket.DownloadFailEvent([objectId]))
    }


    return null;
  }
}

export class Uploader extends FileNet<{objectId:string, data:Uint8Array}> {
  static instance?:Uploader;

  static getInstance() {
    if (!Uploader.instance) {
      Uploader.instance = new Uploader();
    }
    return Uploader.instance;
  }

  addRequest(request: { objectId: string; data: Uint8Array }) {
    if (this.waitQueue_.data().map(item => item.objectId).indexOf(request.objectId)>=0) {
      return ;
    }

    super.addRequest(request);
  }

  protected async _execute(request:{objectId:string, data:Uint8Array}): Promise<Error|null> {
    // const [stsToken, err01] = await GetAliyunSTSToken();
    // if (err01) {
    //   return err01;
    // }
    //
    // const [bucket, err0] = await GetBucketInfo();
    // if (err0) {
    //   return err0;
    // }
    //
    // const client = new OSS({...stsToken, ...bucket});
    // const {objectId, data} = request;
    // await DBBucket.Insert(await DB.GetSelfDB(), {id:objectId, progress:0});
    //
    // do {
    //   try {
    //     const head = await client.head(objectId); // 检查aliOss上是否有该图片，如果有的话则不继续上传
    //     if (head.status!==200) {
    //       break;
    //     }
    //
    //     const err = await DBBucket.SetProgress(await DB.GetSelfDB(), objectId, 100);
    //     if (err) {
    //       break;
    //     }
    //
    //     await getUs().nc.post(new DBBucket.UploadProgressEvent([objectId]));
    //     return null;
    //   } catch (e:any) {
    //     if (e.code !== 'NoSuchKey') { // 如果在oss中没找到，不做任何处理
    //       await getUs().nc.post(new DBBucket.UploadFailEvent([objectId]))
    //     }
    //
    //   }
    // } while (0);
    //
    //
    // try {
    //   const options = {
    //     partSize: 512*1024,
    //     parallel: 3,
    //     progress:async (value:number, checkpoint:OSS.Checkpoint) => {
    //       await DBBucket.SetProgress(await DB.GetSelfDB(), objectId, value*100, JSON.stringify(checkpoint));
    //       await getUs().nc.post(new DBBucket.UploadProgressEvent([objectId]));
    //     },
    //   } as OSS.MultipartUploadOptions;
    //
    //   await client.multipartUpload(objectId, new Blob([data]), options);
    //   return null;
    // } catch (e) {
    //   // Please create a bucket first
    //   Async(async () => {
    //     await getUs().nc.post(new DBBucket.UploadFailEvent([objectId]));
    //   })
    //   return new Error((e as Error).message??"upload fail");
    // }
    return null;
  }
}
