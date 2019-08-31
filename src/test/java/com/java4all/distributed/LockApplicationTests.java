package com.java4all.distributed;

import com.java4all.distributed.constant.ZookeeperProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LockApplicationTests {

	@Autowired
	private ZookeeperProperties zookeeperProperties;

	@Test
	public void contextLoads() {
		System.out.println("aaaaa");
	}


}

