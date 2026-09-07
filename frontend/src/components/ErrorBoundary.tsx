import { Component, type ErrorInfo, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  error: Error | null
}

/**
 * 렌더링 중 잡히지 않은 에러를 여기서 막지 않으면 리액트가 트리 전체를 unmount해서
 * 흰 화면만 남는다. 대신 재시도 버튼이 있는 화면을 보여준다.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled render error', error, info.componentStack)
  }

  render() {
    if (this.state.error) {
      return (
        <div className="flex h-full min-h-screen flex-col items-center justify-center gap-4 bg-canvas px-6 text-center">
          <p className="text-base font-semibold text-ink">문제가 발생했어요.</p>
          <p className="text-sm text-ink-soft">
            페이지를 불러오는 중 오류가 났어요. 새로고침해서 다시 시도해주세요.
          </p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="rounded-xl bg-accent px-5 py-2.5 text-sm font-semibold text-white"
          >
            새로고침
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
