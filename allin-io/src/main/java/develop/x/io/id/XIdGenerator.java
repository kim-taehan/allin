package develop.x.io.id;

import com.github.f4b6a3.tsid.TsidCreator;

/**
 * transactionId 생성 유틸리티.
 *
 * <p>UUID(36자)는 {@code XHeader} transactionId 필드 폭(20)에 직렬화할 수 없으므로,
 * Snowflake 계열의 시간정렬 64비트 ID(TSID)를 사용한다. TSID의 canonical 표현은
 * Crockford base32 13자 고정(부호 없음, URL-safe, 시간정렬)이라 폭 20에 여유 있게 적합하며,
 * {@code toLong()} 의 부호 비트로 인한 음수/시간 경과 이슈가 없다.
 *
 * <p>다중 인스턴스 배포 시 노드ID 충돌로 인한 ID 중복을 방지하려면, 인스턴스별로
 * {@code tsidcreator.node} 시스템 프로퍼티 또는 {@code TSIDCREATOR_NODE} 환경변수로
 * 고유한 노드ID를 지정해야 한다.
 */
public final class XIdGenerator {

    private XIdGenerator() {
    }

    /**
     * 새 transactionId 를 Crockford base32 문자열로 반환한다(13자 고정, 폭 20 적합).
     *
     * <p>{@link TsidCreator} 는 스레드 안전하므로 동기화 없이 호출할 수 있다.
     *
     * @return TSID 기반 Crockford base32 transactionId (13자, 부호 없음, 시간정렬)
     */
    public static String nextTransactionId() {
        return TsidCreator.getTsid().toString();
    }
}
