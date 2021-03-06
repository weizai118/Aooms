package net.aooms.core.web.render;

import javax.servlet.http.HttpServletResponse;

/**
 * 文本渲染
 * Created by 风象南(yuboon) on 2018-04-20
 */
public class TextRender extends AbstractRender {

    public TextRender() {
        this.renderType = RenderType.TEXT;
    }

    @Override
    public void render(HttpServletResponse response, Object value) throws Exception {
        response.setContentType(renderType.getContentType());
        //response.getWriter().write(String.valueOf(value));
        //this.flushAndClose(response);
        this.springMvcRender(value);
    }

}