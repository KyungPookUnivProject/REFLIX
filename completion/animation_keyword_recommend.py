import argparse
import json
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.neighbors import NearestNeighbors


def recommend_movies_by_keywords(keywords):
    # 데이터셋 로드
    df = pd.read_csv('Animation_final.csv')
    

    # TF-IDF 벡터화
    tfidf = TfidfVectorizer(stop_words='english')
    tfidf_matrix = tfidf.fit_transform(df['keywords'].values.astype('U'))

    # NearestNeighbors 모델 생성 및 학습
    nn = NearestNeighbors(metric='cosine', algorithm='brute')
    nn.fit(tfidf_matrix)

    # 입력 데이터 TF-IDF 벡터화
    input_data = tfidf.transform([" ".join(keywords)])

    # 유사한 영화 추천
    similarity_scores, content_indices = nn.kneighbors(input_data, n_neighbors=10)

    # 추천 컨텐츠 출력
    recommended_content = df.iloc[content_indices[0]][['tmdbId','title']].to_dict('records')
    return json.dumps(recommended_content)



if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Movie recommendation system')
    parser.add_argument('keywords', type=str, nargs='+', help='List of keywords')
    args = parser.parse_args()

    print(recommend_movies_by_keywords(args.keywords))
