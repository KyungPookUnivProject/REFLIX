import pandas as pd
import sys
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import json

def load_data(filename):
    data = pd.read_csv(filename)
    
    # 평점 투표수 기준 상위 50%의 영화만 추출
    m = data['rating_count'].quantile(0.8)
    data = data.loc[data['rating_count'] >= m].copy()

    # 가중평균 계산
    C = data['rating_avg'].mean()
    def weighted_rating(x, m=m, C=C):
        v = x['rating_count']
        R = x['rating_avg']
        return (v/(v+m) * R) + (m/(m+v) * C)
    data['score'] = data.apply(weighted_rating, axis=1)
    
    # CountVectorizer를 사용하여 장르별 유사도 계산
    count_vector = CountVectorizer(ngram_range=(1, 3))
    c_vector_genres = count_vector.fit_transform(data['genre'])
    genre_c_sim = cosine_similarity(c_vector_genres, c_vector_genres).argsort()[:, ::-1]
    
    return data, genre_c_sim

tv_data, tv_genre_c_sim = load_data("movie_final.csv")

# 장르를 넣으면 랜덤으로 그 장르의 영화 10가지 리턴.
def random_genres_items_tv(genre):
    # df = pd.read_csv("Animation_final.csv")
    genre_df = tv_data[tv_data['genre'].apply(lambda x: genre in x)]
    
    genre_df = genre_df.sort_values(by='score', ascending=False).head(10)
    genre_df = genre_df.fillna('')
    
    result_items = genre_df[['tmdbId', 'title']].to_dict("records")
        
    return result_items
    # genre_df = genre_df.fillna('')
    
    # if len(genre_df) < 10:
    #     result_items = genre_df[['content_id', 'title','poster_path']].to_dict("records")
    # else:
    #     result_items = genre_df[['content_id', 'title', 'poster_path']].sample(n=10).to_dict("records")
        
    # return result_items

arg1 = sys.argv[1]
result = random_genres_items_tv(arg1)
json_result = json.dumps(result)

print(json_result)