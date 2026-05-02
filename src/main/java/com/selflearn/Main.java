package com.selflearn;

import com.selflearn.labs.hashmap.HashMapInternalsLab;
import com.selflearn.labs.hashmap.PrerequisitesRunner;
import com.selflearn.labs.networkfoundations.NetworkFoundationsEmbeddedDemo;
import com.selflearn.labs.networkfoundations.RetryBackoffJitterLab;
import com.selflearn.labs.networkfoundations.TokenBucketRateLimiterLab;
import com.selflearn.labs.networkfoundations.HedgedRequestsLab;
import com.selflearn.labs.networkfoundations.TracePropagationDemo;
import com.selflearn.labs.oopprinciples.OopPrinciplesDemo;

/**
 * Entry point for hands-on labs. Each topic can add a runner under {@code com.selflearn.labs.*}
 * or use Gradle tasks like {@code runHashMapLab}.
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "hashmap".equalsIgnoreCase(args[0])) {
            HashMapInternalsLab.main(new String[0]);
            return;
        }
        if (args.length > 0 && "prereq".equalsIgnoreCase(args[0])) {
            PrerequisitesRunner.main(new String[0]);
            return;
        }
        if (args.length > 0 && "net".equalsIgnoreCase(args[0])) {
            NetworkFoundationsEmbeddedDemo.main(new String[0]);
            return;
        }
        if (args.length > 0 && "retrylab".equalsIgnoreCase(args[0])) {
            RetryBackoffJitterLab.main(new String[0]);
            return;
        }
        if (args.length > 0 && "ratelimitlab".equalsIgnoreCase(args[0])) {
            TokenBucketRateLimiterLab.main(new String[0]);
            return;
        }
        if (args.length > 0 && "hedgelab".equalsIgnoreCase(args[0])) {
            HedgedRequestsLab.main(new String[0]);
            return;
        }
        if (args.length > 0 && "tracelab".equalsIgnoreCase(args[0])) {
            TracePropagationDemo.main(new String[0]);
            return;
        }
        if (args.length > 0 && "oop".equalsIgnoreCase(args[0])) {
            OopPrinciplesDemo.main(new String[0]);
            return;
        }
        System.out.println("self-learn | Java " + Runtime.version());
        System.out.println("  ./gradlew runPrerequisitesExamples — before HashMapInternalsLab");
        System.out.println("  ./gradlew runHashMapLab           — Day 1 HashMap internals");
        System.out.println("  ./gradlew runNetworkFoundationsDemo — Foundation First (internet + HTTP)");
        System.out.println("  ./gradlew runRetryBackoffJitterLab");
        System.out.println("  ./gradlew runTokenBucketRateLimiterLab");
        System.out.println("  ./gradlew runHedgedRequestsLab");
        System.out.println("  ./gradlew runTracePropagationDemo");
        System.out.println("  ./gradlew runOopPrinciplesDemo");
        System.out.println("  ./gradlew run --args prereq|hashmap|net|retrylab|ratelimitlab|hedgelab|tracelab|oop");
    }
}
