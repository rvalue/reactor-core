/*
 * Copyright (c) 2011-2018 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;

/**
 * A simple {@link Disposable} symmetric {@link Processor} (taking the same type as what
 * it outputs), with {@link Flux} like semantics (0-N elements).
 *
 * @author Simon Baslé
 */
public interface BalancedFluxProcessor<T>
		extends Processor<T, T>, Disposable, CoreSubscriber<T> {

	/**
	 * Return the produced {@link Throwable} error if any or null
	 *
	 * @return the produced {@link Throwable} error if any or null
	 */
	@Nullable
	Throwable getError();

	/**
	 * Return true if terminated with onComplete
	 *
	 * @return true if terminated with onComplete
	 */
	default boolean hasCompleted() {
		return isTerminated() && getError() == null;
	}

	/**
	 * Return true if terminated with onError
	 *
	 * @return true if terminated with onError
	 */
	default boolean hasError() {
		return isTerminated() && getError() != null;
	}

	//TODO isCancelled
//	/**
//	 * Indicates whether this {@link BalancedFluxProcessor} has been interrupted via cancellation.
//	 *
//	 * @return {@code true} if this {@link BalancedFluxProcessor} is cancelled, {@code false}
//	 * otherwise.
//	 */
//	boolean isCancelled();

	/**
	 * Indicates whether this {@link BalancedFluxProcessor} has been terminated by the
	 * source producer with a success or an error.
	 *
	 * @return {@code true} if this {@link BalancedFluxProcessor} is successful, {@code false} otherwise.
	 */
	boolean isTerminated();

	/**
	 * Return the number of active {@link Subscriber} or {@literal -1} if untracked.
	 *
	 * @return the number of active {@link Subscriber} or {@literal -1} if untracked
	 */
	long downstreamCount();

	/**
	 * Return true if any {@link Subscriber} is actively subscribed
	 *
	 * @return true if any {@link Subscriber} is actively subscribed
	 */
	default boolean hasDownstreams() {
		return downstreamCount() != 0L;
	}

	/**
	 * Return true if this {@link BalancedFluxProcessor} supports multithread producing
	 *
	 * @return true if this {@link BalancedFluxProcessor} supports multithread producing
	 */
	boolean isSerialized();

	/**
	 * Create a {@link FluxSink} that safely gates multi-threaded producer
	 * {@link Subscriber#onNext(Object)}. This processor will be subscribed to
	 * said {@link FluxSink}, and any previous subscribers will be unsubscribed.
	 *
	 * <p> The returned {@link FluxSink} will not apply any
	 * {@link FluxSink.OverflowStrategy} and overflowing {@link FluxSink#next(Object)}
	 * will behave in two possible ways depending on the Processor:
	 * <ul>
	 * <li> an unbounded processor will handle the overflow itself by dropping or
	 * buffering </li>
	 * <li> a bounded processor will block/spin</li>
	 * </ul>
	 *
	 * @return a serializing {@link FluxSink}
	 */
	FluxSink<T> sink();

	/**
	 * Create a {@link FluxSink} that safely gates multi-threaded producer
	 * {@link Subscriber#onNext(Object)}. This processor will be subscribed to
	 * said {@link FluxSink}, and any previous subscribers will be unsubscribed.
	 *
	 * <p> The returned {@link FluxSink} will deal with overflowing {@link FluxSink#next(Object)}
	 * according to the selected {@link reactor.core.publisher.FluxSink.OverflowStrategy}.
	 *
	 * @param strategy the overflow strategy, see {@link FluxSink.OverflowStrategy}
	 * for the available strategies
	 * @return a serializing {@link FluxSink}
	 */
	FluxSink<T> sink(FluxSink.OverflowStrategy strategy);

	/**
	 * Expose a {@link Flux} API on top of the {@link BalancedFluxProcessor}'s output,
	 * allowing composition of operators on it.
	 *
	 * @implNote most implementations will already implement {@link Flux} and thus can
	 * return themselves.
	 *
	 * @return a {@link Flux} API on top of the {@link Processor}'s output
	 */
	Flux<T> asFlux();
}