package develop.x.simulator.game.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueryOption {
    A001("고정 환급률/배당률 투표권"),
    A002("고정 환급률 게임 종목별 정보"),
    A003("고정 배당률 기록식 종목별 정보"),
    A004("고정 배당률 승부식 종목별 정보"),
    A006("고정 환급률 발매가능 회차 목록"),
    A007("고정 환급률/배당률 투표권"),
    A008("고정 배당률 발매가능 회차 목록")
    ;
    private final String text;
}
