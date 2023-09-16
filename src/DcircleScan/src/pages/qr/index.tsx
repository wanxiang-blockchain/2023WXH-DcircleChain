import {useNavigate, useParams} from 'react-router-dom';
import {useEffect} from "react";
import {AddressFactory, AddressType} from "./addressFactory";
import {getHttp, saveHttp} from "../../helper/handleUrl";
import {Async} from "../../Async";

export function Qr() {
  const { userAddress,typeVal } = useParams();
  const navigate = useNavigate();
  useEffect(() => {
    if (!userAddress) {
      Async(async () => {
        const factory = AddressFactory.build(AddressType.default)
        factory.redirectTo(navigate, await factory.getAddress(''))
      })
      return;
    };
    if (getHttp().length > 0) return;
    handleBase64Address(userAddress).then();
  }, [userAddress])

  const handleBase64Address = async (userAddress:string) => {
    saveHttp(window.location.href);
    Async(async () => {
      try{
        await navigator.clipboard.writeText(window.location.href)
      } catch (err) {
        console.log(err, '复制失败')
      }
    })
    const factory = AddressFactory.build(typeVal as AddressType)
    factory.redirectTo(navigate, await factory.getAddress(userAddress))
  }

  return (<div></div>)
}
