package com.example.todaystyle.common;

import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * 리액트 라우터(client-side routing) SPA를 위한 정적 리소스 폴백. 실제 파일(JS/CSS/이미지 등)로
 * 매칭되는 요청은 그대로 서빙하고, 매칭 안 되는 경로(예: 브라우저에서 /mypage를 새로고침하는 경우)는
 * index.html로 대신 응답해서 프론트 라우터가 화면 전환을 이어받게 한다. /api/**는 REST 컨트롤러가
 * 더 높은 우선순위로 먼저 처리하므로 여기까지 오지 않는다.
 *
 * <p><b>주의:</b> 존재하지 않는 파일이라고 무조건 index.html로 폴백하면 안 된다 — 예전 빌드를
 * 캐시(브라우저 캐시 또는 PWA 서비스워커 프리캐시)해둔 클라이언트가 이제는 없는 해시된
 * 자산 파일(예: /assets/index-OLDHASH.js)을 요청했을 때, 진짜 404 대신 이 폴백이 index.html을
 * 200으로 돌려주면 브라우저가 "JS인 줄 알았는데 HTML"이라며 MIME 타입 불일치로 실행을 거부해
 * 리액트가 마운트조차 못 하고 흰 화면만 남는다. 그래서 파일 확장자가 있는 경로(정적 자산 요청)는
 * 못 찾으면 그대로 404를 내고, 확장자가 없는 경로(클라이언트 라우트)만 index.html로 폴백한다.
 */
class SpaFallbackResourceResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        Resource requestedResource = location.createRelative(resourcePath);
        if (requestedResource.exists() && requestedResource.isReadable()) {
            return requestedResource;
        }
        if (looksLikeStaticFileRequest(resourcePath)) {
            return null;
        }
        return new ClassPathResource("/static/index.html");
    }

    private boolean looksLikeStaticFileRequest(String resourcePath) {
        int lastSlash = resourcePath.lastIndexOf('/');
        String lastSegment = lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
        return lastSegment.contains(".");
    }
}
