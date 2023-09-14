export default function formatTime(times:number) {
  if (times === 0) {
    return '0000/00/00 00:00:00'
  }
  let date = new Date(times);
  let year = date.getFullYear();
  let month:number|string = date.getMonth() + 1;
  if (month < 10) month = `0${month}`
  let day:number|string = date.getDate();
  if (day < 10) day = `0${day}`

  let h:number|string = date.getHours();
  if (h < 10) h = `0${h}`
  let m:number|string = date.getMinutes();
  if (m < 10) m = `0${m}`
  let s:number|string = date.getSeconds();
  if (s < 10) s = `0${s}`

  return `${year}/${month}/${day} ${h}:${m}:${s}`
}