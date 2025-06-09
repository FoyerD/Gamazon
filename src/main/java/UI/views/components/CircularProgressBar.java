package UI.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

@Tag("div")
public class CircularProgressBar extends Component {

    public CircularProgressBar(int approved, int total) {
        double percent = total == 0 ? 0 : ((100.0 * approved) / total);
        String percentText = String.format("%.0f%%", percent);

        String svg = """
            <svg width="80" height="80" viewBox="0 0 36 36">
                <style>
                    .circle-bg {
                        fill: none;
                        stroke: #eee;
                        stroke-width: 3.8;
                    }
                    .circle {
                        fill: none;
                        stroke: #00bc8c;
                        stroke-width: 3.8;
                        stroke-linecap: round;
                        transition: stroke-dasharray 0.5s ease;
                    }
                    .percentage {
                        fill: #333;
                        font-size: 5px;
                        text-anchor: middle;
                    }
                </style>
                <path
                    class="circle-bg"
                    d="M18 2.0845
                       a 15.9155 15.9155 0 0 1 0 31.831
                       a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                    class="circle"
                    stroke-dasharray="%.1f, 100"
                    d="M18 2.0845
                       a 15.9155 15.9155 0 0 1 0 31.831
                       a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <text x="18" y="20.35" class="percentage">%s</text>
            </svg>
        """.formatted(percent, percentText);

        Element element = getElement();
        element.setProperty("innerHTML", svg);
        element.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");
    }
}
