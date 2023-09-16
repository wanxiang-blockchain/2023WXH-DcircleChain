package websocket

import "testing"

func TestEscapeReg(t *testing.T) {
  data := []struct {
    str    string
    expect string
  }{{
    "*", "^.*$",
  },
  {
    "*com", "^.*com$",
  },
  {
    "www.baidu.com", "^www\\.baidu\\.com$",
  },
  {
    "*.baidu.com?a=1&b=2", "^.*\\.baidu\\.com\\?a=1&b=2$",
  },
  {
    "user.*.baidu.com", "^user\\..*\\.baidu\\.com$",
  },
  {
    "user.*.baidu.com?a=1&b=2", "^user\\..*\\.baidu\\.com\\?a=1&b=2$",
  },
  {
    "adddd", "^adddd$",
  },
  }

  for _,d := range data {
    e := escapeReg(d.str)
    if d.expect != e {
      t.Errorf("%s error. expect: %s, actual: %s", d.str, d.expect, e)
    }
  }
}
