export default function formatNum(nums:number|string) {
  const numType = getType(nums)
  if (numType === 'number') {
    return nums === 0 ? '-' : nums
  }
  if (numType === 'string') {
    return (nums as string).length <= 0 ? '-' : nums
  }
  return '未知类型';
}

function getType(query:string |number) {

  const typeMap = new Map([

    ["[object Number]", 'number'],

    ["[object String]", 'string']

  ]);

  if (!typeMap.has(Object.prototype.toString.call(query))) {

    return '暂不支持当前类型判断';

  }

  return typeMap.get(Object.prototype.toString.call(query))

}