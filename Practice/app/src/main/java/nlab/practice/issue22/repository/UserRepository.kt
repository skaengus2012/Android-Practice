package nlab.practice.issue22.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import nlab.practice.common.PracticeDataBase
import nlab.practice.issue22.MockWebService
import nlab.practice.issue22.model.User
import nlab.practice.issue22.viewModel.UserMutableLive

/**
 * User 정보를 조회하고, ViewModel 에 업데이트하는 클래스 레벨 정의.
 *
 * @author Doohyun
 */
object UserRepository {

    /**
     * [userId] 를 이용하여, 데이터를 조회하는 함수 정의
     *
     * @param userId
     * @return
     */
    fun getUser(userId : String) : UserMutableLive {
        val result = UserMutableLive()

        Observable.create<User> {
                    val user = if (isEmptyInRoom(userId)) {
                        val data = readUserByMock(userId)
                        saveUserInRoom(data)

                        data
                    } else {
                        readUserByRoom(userId)
                    }

                    user?.run { it.onNext(this) }
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result.value = it })

        return result
    }

    /**
     * [userId] 를 이용하여, 목에서 데이터 조회.
     *
     * @param userId
     * @param resultFunction
     * @return
     */
    inline fun refreashUser(userId : String, crossinline resultFunction : (user : User) -> Unit) {
        Observable.create<User> {
                    val user = MockWebService.getUser(userId)

                    if (isEmptyInRoom(userId)){
                        saveUserInRoom(user)
                    } else {
                        update(user)
                    }

                    it.onNext(user)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resultFunction(it) })
    }

    /**
     * [user] 를 저장
     *
     * @param user
     */
    fun saveUserInRoom(user : User) = PracticeDataBase.getInstance().userDao().insert(user)

    /**
     * [user] 를 이용하여, 업데이트 처리.
     *
     * @param user
     */
    fun update(user: User) {
        Observable.create<Void> {
                    PracticeDataBase.getInstance().userDao().update(user)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    /**
     * [userId] 를 이용하여, Mock 에서 데이터를 조회한다.
     *
     * @param userId
     * @return
     */
    fun readUserByMock(userId : String) : User = MockWebService.getUser(userId)

    /**
     * [userId] 를 이용히여, Room 에서 데이터를 조회.
     *
     * @param userId
     * @return
     */
    fun readUserByRoom(userId : String) : User? = PracticeDataBase.getInstance().userDao().find(userId)

    /**
     * [userId] 에 해당하는 정보가 존재하는가?
     *
     * @param userId
     * @return
     */
    fun isEmptyInRoom(userId : String) : Boolean = PracticeDataBase.getInstance().userDao().getCount(userId) == 0
}