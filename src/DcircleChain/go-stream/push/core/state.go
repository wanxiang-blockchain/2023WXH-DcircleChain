package core

type State byte

const (
  Success State = iota
  HostNameErr
  TokenNotExist
  ServerInternalErr
  Timeout
)

func StateText(s State) string {
  switch s {
  case Success:
    return "Success"
  case HostNameErr:
    return "HostNameErr"
  case TokenNotExist:
    return "TokenNotExist"
  case ServerInternalErr:
    return "ServerInternalErr"
  case Timeout:
    return "Timeout"
  default:
    return "Unknown"
  }
}

func (s State) String() string {
  return StateText(s)
}

func (s State) ToInt() int {
  return int(s)
}
