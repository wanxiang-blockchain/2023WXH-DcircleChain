import React, {createContext, useEffect, useState} from 'react';
import {Routes, Route, useLocation, useNavigate} from 'react-router-dom';
import {User} from "./pages/user";
import {Group} from "./pages/group";
import {Article} from "./pages/article";
import {Transaction} from "./pages/transation";
import {TransactionGroup} from "./pages/transationGroup";
import {Qr} from "./pages/qr"
import {Download} from "./pages/download"
import './App.css';
import {setStreamClientConstructor} from "./3rdparty/ts-baselib";
import {Streamer} from "./Streamer";
import {CheckVersion} from "./api/CheckVersion";
import {ConfigProvider} from "antd";
import {IntlProvider} from "react-intl";
import enUS from "antd/lib/locale/en_US";
import zhCN from "antd/lib/locale/zh_CN";
import zhTW from "antd/lib/locale/zh_TW";
import en from "./language/en.json";
import zh from "./language/zh-CN.json";
import zhT from "./language/zh-CN-rTw.json"
import {LangType} from "./helper/intlFormatMessage";
import {setLocation, setNavigate} from "./helper/handleUrl";

setStreamClientConstructor(Streamer);

export const SetLanguage = createContext<{
  setLanguage: Function
}>({
  setLanguage: (language:string) => {}
});

function App() {
  const location = useLocation();
  const navigate = useNavigate();
  useEffect(() => {
    CheckVersion().then();

  }, []);
  const setLanguage = (language: LangType) => {
    if (currentLanguage !== language && language) {
      setCurrentLanguage(language)
    }
  }

  const messages = {
    'en': en,
    'zhHans': zh,
    'zhHant': zhT
  };
  const [currentLanguage, setCurrentLanguage] = useState<LangType>(LangType.US);

  const buildLanguage = (language:LangType) => {
    const languageMap = new Map([
      [LangType.US, enUS],
      [LangType.CN, zhCN],
      [LangType.TW, zhTW],
    ]);
    if (!languageMap.has(language)) {
      return languageMap.get(LangType.US)
    }
    return languageMap.get(language);
  }

  useEffect(() => {
    setNavigate(navigate);
    setLocation(location)
    const path = location.pathname;
    if (path.length <= 0) return;
    let dom = document.getElementById('tipsDownload');
    if (!dom) return;
    if (path.indexOf('download') < 0) {
      dom.style.display = "flex"
      return;
    }
    if (!location.state || location.state.click === 'allow') {
      dom.style.display = "flex"
    } else {
      dom.style.display = "none"
    }
  }, [location]);

  return (
    <SetLanguage.Provider
      value={{
        setLanguage: setLanguage
      }}
    >
      <ConfigProvider locale={buildLanguage(currentLanguage)}>
        <IntlProvider locale={currentLanguage} messages={messages[currentLanguage]}>
          <Routes>
            <Route path="/qr/:typeVal/:userAddress?/:language?" element={<Qr />}></Route>
            <Route path="/download" element={<Download />}></Route>
            <Route path="/user/:userAddress/:language?" element={<User />} />
            <Route path="/user/:userAddress/tab/:tab/:language?" element={<User />} />
            <Route path="/group/:groupAddress/:language?" element={<Group />} />
            <Route path="/article/:articleAddress/:language?" element={<Article />} />
            <Route path="/transaction/:transactionHash/:language?" element={<Transaction />} />
            <Route path="/transactionGroup/:transactionHash/:language?" element={<TransactionGroup />} />
          </Routes>
        </IntlProvider>
      </ConfigProvider>
  </SetLanguage.Provider>

  );
}

export default App;
