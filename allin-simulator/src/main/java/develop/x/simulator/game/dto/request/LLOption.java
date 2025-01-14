package develop.x.simulator.game.dto.request;


import develop.x.simulator.Selection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LLOption implements Selection {
    WIN, DRAW, LOSE;

    @Override
    public String displayName() {
        return name();
    }
}
