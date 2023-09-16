import {NavigateFunction, Location} from "react-router-dom";

let http:string = '';
let navigate:NavigateFunction|null = null;
let location:Location|null = null;

export function handleQueryLinkAndSaveHttp(query: string) {
  if (query.length <= 0) return;
  const url = query.substring(1);
  const arr = url.split("=");
  if (arr.length !== 2) return;
  if (arr[0] !== 'link') return;
  const realUrl = decodeURIComponent(arr[1]);
  saveHttp(realUrl);
}
export function saveHttp(url:string) {
  if (!url || url.length <= 0) return;
  http = url;
}

export function getHttp() {
  return http;
}

export function setNavigate(navigater:NavigateFunction) {
  if (navigate) return;
  navigate = navigater
}

export function  getNavigate() {
  if (!navigate) {
    return null;
  }
  return navigate;
}

export function setLocation(loca: Location) {
  if (loca) return;
  location = loca
}

export function  getLocation() {
  if (!location) {
    return null;
  }
  return location;
}

