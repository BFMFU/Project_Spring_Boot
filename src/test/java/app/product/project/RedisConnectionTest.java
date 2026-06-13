package app.product.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisConnectionTest {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	void test() {
		redisTemplate.opsForValue().set("test", "hello");
		System.out.println(redisTemplate.opsForValue().get("test"));
	}
}